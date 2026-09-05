package io.github.solclient.client.ui.component.impl;

import io.github.solclient.client.mod.impl.SolClientConfig;
import io.github.solclient.client.platform.mc.render.GlStateManager;
import io.github.solclient.client.platform.mc.util.Input;
import io.github.solclient.client.ui.component.*;
import io.github.solclient.client.ui.component.controller.AlignedBoundsController;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.*;

public abstract class ScrollListComponent extends Component {

	private double targetY;
	private double animatedY;
	private double lastAnimatedY;
	private double calculatedY;
	private int maxScrolling;
	private double scrollPercent;
	private Component scrollbar;
	private int scrolling = -1;
	private int lastMouseY;
	private double grabStartY = -1;
	private int grabMouseY;

	@Override
	public void setParent(Component parent) {
		Component previousParent = getParent();
		if(previousParent == parent && (parent == null
				|| scrollbar != null && scrollbar.getParent() == parent)) {
			return;
		}

		if(previousParent != null && scrollbar != null && scrollbar.getParent() == previousParent) {
			previousParent.remove(scrollbar);
		}

		if(parent == null) {
			super.setParent(null);
			return;
		}

		super.setParent(parent);

		parent.add(scrollbar = new BlockComponent(Theme.buttonSecondary(), 2F) {

			@Override
			public void render(ComponentRenderInfo info) {
				int contentHeight = getContentHeight();
				scrollPercent = contentHeight <= 0 ? 0.0
						: (double) (ScrollListComponent.this.getBounds().getHeight() - 6) / contentHeight;

				GlStateManager.translate(0, calculatedY * scrollPercent, 0);

				if(maxScrolling != 0) {
					super.render(info);
				}
			}

		}, (component, defaultBounds) -> {
			return new Rectangle(getBounds().getX() + getBounds().getWidth() - 5, getBounds().getY(), 3,
					(int) (getBounds().getHeight() * scrollPercent));
		});
	}

	public void add(Component component) {
		add(component, new AlignedBoundsController(Alignment.CENTRE, Alignment.START, (sizingComponent, defaultBounds) -> {
			Component lastAdded = null;
			Rectangle lastBounds = null;

			int index = subComponents.indexOf(component) - 1;

			if(index > -1) {
				lastAdded = subComponents.get(index);
				lastBounds = lastAdded.getBounds();
			}

			return new Rectangle(defaultBounds.getX(),
					lastAdded == null || lastBounds == null ? 0
							: lastBounds.getY() + lastBounds.getHeight() + getSpacing(),
					defaultBounds.getWidth(), defaultBounds.getHeight());
		}));
	}

	private ComponentRenderInfo translate(ComponentRenderInfo info) {
		return new ComponentRenderInfo(info.getRelativeMouseX(), (int) (info.getRelativeMouseY() + calculatedY), info.getPartialTicks());
	}

	public ComponentRenderInfo reverseTranslation(ComponentRenderInfo info) {
		return new ComponentRenderInfo(info.getRelativeMouseX(), (int) (info.getRelativeMouseY() - calculatedY), info.getPartialTicks());
	}

	@Override
	public void render(ComponentRenderInfo info) {
		if(!SolClientConfig.INSTANCE.smoothScrolling) {
			calculatedY = targetY;
		}
		else {
			calculatedY = (lastAnimatedY + (animatedY - lastAnimatedY) * info.getPartialTicks());
		}

		GlStateManager.translate(0, -calculatedY, 0);
		maxScrolling = getContentHeight() - getBounds().getHeight();

		if(maxScrolling < 0) {
			maxScrolling = 0;
		}

		if(lastMouseY != info.getRelativeMouseY()) {
			if(scrolling > 0) {
				int targetCompY = info.getRelativeMouseY() - scrolling;
				jumpTo((int) (targetCompY / (getBounds().getHeight() / (double) getContentHeight())));
				clamp();
			}
			else if(grabStartY != -1) {
				jumpTo(grabStartY - (info.getRelativeMouseY() - grabMouseY));
				clamp();
			}
		}

		lastMouseY = info.getRelativeMouseY();

		super.render(translate(info));
	}

	private int mouseInScrollbar(ComponentRenderInfo info) {
		Rectangle scrollBounds = scrollbar.getBounds().offset(-getBounds().getX(), -getBounds().getY() + (int) (calculatedY * scrollPercent));

		if(new Rectangle(scrollBounds.getX(), 0, scrollBounds.getWidth(),
				getBounds().getHeight()).contains(info.getRelativeMouseX(), info.getRelativeMouseY())) {
			if(!scrollBounds.contains(info.getRelativeMouseX(), info.getRelativeMouseY())) {
				return scrollbar.getBounds().getHeight() / 2;
			}

			return info.getRelativeMouseY() - scrollBounds.getY();
		}

		return -1;
	}

	@Override
	public boolean mouseClicked(ComponentRenderInfo info, int button) {
		if(button == 0) {
			scrolling = mouseInScrollbar(reverseTranslation(info));
			lastMouseY = -1;
			if(scrolling != -1) {
				return true;
			}
		}

		boolean superResult = super.mouseClicked(info, button);

		if(button == 0 && !superResult && grabStartY == -1) {
			grabStartY = targetY;
			grabMouseY = reverseTranslation(info).getRelativeMouseY();
		}

		return true;
	}

	@Override
	public boolean mouseReleasedAnywhere(ComponentRenderInfo info, int button, boolean inside) {
		if(button == 0) {
			if(scrolling != -1) {
				scrolling = -1;
				return true;
			}
			if(grabStartY != -1) {
				grabStartY = -1;
				return true;
			}
		}

		return super.mouseReleasedAnywhere(info, button, inside);
	}

	@Override
	protected boolean shouldCull(Component component) {
		if(component == null || component.getParent() != this) {
			return true;
		}

		if((component.getBounds().getEndY() - calculatedY) < 0) {
			return true;
		}
		else if(component.getBounds().getY() - calculatedY > getBounds().getHeight()) {
			return true;
		}

		return false;
	}

	public void snapTo(int scroll) {
		targetY = animatedY = lastAnimatedY = calculatedY = maxScrolling = scroll;
	}

	public void jumpTo(double scroll) {
		targetY = scroll;
	}

	public int getScroll() {
		return (int) targetY;
	}

	protected int getContentHeight() {
		for(int index = subComponents.size() - 1; index >= 0; index--) {
			Component component = subComponents.get(index);
			if(component.getParent() == this) {
				return component.getBounds().getEndY() + getSpacing() * 2;
			}
		}

		return 0;
	}

	@Override
	public boolean mouseClickedAnywhere(ComponentRenderInfo info, int button, boolean inside, boolean processed) {
		boolean insideList = inside && info.getRelativeMouseX() > 0
				&& info.getRelativeMouseY() > 0
				&& info.getRelativeMouseX() < getBounds().getWidth()
				&& info.getRelativeMouseY() < getBounds().getHeight();
		return super.mouseClickedAnywhere(translate(info), button, insideList, processed);
	}

	protected int getScrollStep() {
		for(Component component : subComponents) {
			if(component.getParent() == this) {
				return component.getBounds().getHeight() + getSpacing();
			}
		}

		return 0;
	}

	@Override
	public boolean mouseScroll(ComponentRenderInfo info, int delta) {
		if(super.mouseScroll(info, delta)) {
			return true;
		}

			if(delta > 0) {
				delta = 1;
		}
		else if(delta < 0) {
			delta = -1;
		}
			else {
				delta = 0;
			}


		if(subComponents.size() != 0) {
			targetY -= delta * getScrollStep();
		}

		clamp();

		return true;
	}

	@Override
	public boolean keyPressed(ComponentRenderInfo info, int code, int scancode, int mods) {
		if(code == Input.DOWN) {
			targetY += getScrollStep();
		}
		else if(code == Input.UP) {
			targetY -= getScrollStep();
		}

		return false;
	}

	@Override
	public void tick() {
		super.tick();

		clamp();

		lastAnimatedY = animatedY;
		double multiplier = 0.6F;
		animatedY += (targetY - animatedY) * multiplier;
	}

	private void clamp() {
		targetY = Utils.clamp(targetY, 0, maxScrolling);
	}

	@Override
	protected boolean shouldScissor() {
		return true;
	}

	public abstract int getSpacing();

}
