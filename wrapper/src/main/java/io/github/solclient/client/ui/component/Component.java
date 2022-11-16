package io.github.solclient.client.ui.component;

import java.util.*;
import java.util.function.BiPredicate;

import org.lwjgl.opengl.GL11;

import io.github.solclient.client.platform.mc.*;
import io.github.solclient.client.platform.mc.render.GlStateManager;
import io.github.solclient.client.platform.mc.text.Font;
import io.github.solclient.client.ui.component.controller.*;
import io.github.solclient.client.ui.component.handler.*;
import io.github.solclient.client.ui.component.impl.ScrollListComponent;
import io.github.solclient.client.util.*;
import io.github.solclient.client.util.data.*;
import lombok.*;

// Class for GUI components.
// May be replaced by Aether UI in the future when it's stable enough.
// Edit: probably won't be
public abstract class Component {

	protected final MinecraftClient mc = MinecraftClient.getInstance();
	protected Screen screen;
	@Getter
	protected Font font;
	@Getter
	@Setter
	protected Component parent;
	@Getter
	protected boolean hovered;
	protected ClickHandler onClick;
	protected Runnable onMouseEnter;
	protected Runnable onMouseExit;
	@Getter
	protected final List<Component> subComponents = new ArrayList<>();
	private final Map<Component, Controller<Rectangle>> subComponentControllers = new HashMap<>();
	@Getter
	private Component dialog;
	private final AnimatedColourController overlayColour = new AnimatedColourController(
			(component, defaultColour) -> dialog == null ? Colour.TRANSPARENT : new Colour(0, 0, 0, 150));

	private ClickHandler onRelease;
	private KeyHandler onKeyPressed;
	private CharacterHandler onCharacterTyped;
	private ClickHandler onClickAnywhere;
	private ClickHandler onReleaseAnywhere;

	private Controller<Boolean> visibilityController;

	public void add(Component component, Controller<Rectangle> position) {
		subComponents.add(component);
		subComponentControllers.put(component, position);

		if(screen != null) {
			component.setScreen(screen);
		}

		if(font != null) {
			component.setFont(font);
		}

		component.setParent(this);
	}

	public void remove(Component component) {
		subComponents.remove(component);
	}

	public void clear() {
		subComponents.clear();
	}

	public void setFont(Font font) {
		this.font = font;

		for(Component component : subComponents) {
			component.setFont(font);
		}
	}

	public void setScreen(Screen screen) {
		this.screen = screen;

		for(Component component : subComponents) {
			component.setScreen(screen);
		}
	}

	public Rectangle getRelativeBounds() {
		return new Rectangle(0, 0, getBounds().getWidth(), getBounds().getHeight());
	}

	public Rectangle getBounds() {
		return parent.getBounds(this);
	}

	public Rectangle getBounds(Component component) {
		return subComponentControllers.get(component).get(component, component.getDefaultBounds());
	}

	private static ComponentRenderInfo transform(ComponentRenderInfo info, Rectangle bounds) {
		return new ComponentRenderInfo(info.getRelativeMouseX() - bounds.getX(),
				info.getRelativeMouseY() - bounds.getY(), info.getPartialTicks());
	}

	public void render(ComponentRenderInfo info) {
		ComponentRenderInfo actualInfo = info;

		if(this instanceof ScrollListComponent) {
			actualInfo = ((ScrollListComponent) this).reverseTranslation(info);
		}

		hovered = actualInfo.getRelativeMouseX() > 0 && actualInfo.getRelativeMouseY() > 0
				&& actualInfo.getRelativeMouseX() < getBounds().getWidth()
				&& actualInfo.getRelativeMouseY() < getBounds().getHeight();

		if(parent != null) {
			hovered = hovered && (parent.isHovered() || parent.dialog == this);
		}

		if(dialog != null) {
			hovered = false;
		}

		for(Component component : subComponents) {
			if(component == dialog && dialog != null) {
				drawDialogOverlay();
			}

			if(component.shouldSkip() || (shouldScissor() && shouldCull(component))) {
				continue;
			}

			Rectangle bounds = getBounds(component);

			try(ScopeGuard _p = GlScopeGuards.push()) {
				GlStateManager.translate(bounds.getX(), bounds.getY(), 0);

				if(component.shouldScissor()) {
					GL11.glEnable(GL11.GL_SCISSOR_TEST);
					Utils.scissor(bounds);
				}

				component.render(transform(info, bounds));
			}

			if(component.shouldScissor()) {
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			}
		}

		if(dialog == null) {
			drawDialogOverlay();
		}
	}

	private boolean shouldSkip() {
		return visibilityController != null && !visibilityController.get(this, true);
	}

	protected boolean shouldCull(Component component) {
		if(component.getBounds().getEndY() < getBounds().getY()) {
			return true;
		}
		else if(component.getBounds().getY() > getBounds().getHeight()) {
			return true;
		}

		return false;
	}

	private void drawDialogOverlay() {
		GlStateManager.resetColour();
		DrawableHelper.fillRect(0, 0, screen.getWidth(), screen.getHeight(), overlayColour.get(this, Colour.WHITE).getValue());
	}

	protected Rectangle getDefaultBounds() {
		return Rectangle.ZERO;
	}

	protected boolean shouldScissor() {
		return false;
	}

	/**
	 * @return <code>true</code> if event has been processed.
	 */
	public boolean keyPressed(ComponentRenderInfo info, int code, int scancode, int mods) {
		if(onKeyPressed != null && onKeyPressed.keyPressed(info, code, scancode, mods)) {
			return true;
		}

		for(Component component : subComponents) {
			if(component.shouldSkip()) {
				continue;
			}

			if(component.keyPressed(transform(info, getBounds(component)), code, scancode, mods)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return <code>true</code> if event has been processed.
	 */
	public boolean characterTyped(ComponentRenderInfo info, char character) {
		if(onCharacterTyped != null && onCharacterTyped.characterTyped(info, character)) {
			return true;
		}

		for(Component component : subComponents) {
			if(component.shouldSkip()) {
				continue;
			}

			if(component.characterTyped(transform(info, getBounds(component)), character)) {
				return true;
			}
		}

		return false;
	}

	public boolean mouseClickedAnywhere(ComponentRenderInfo info, int button, boolean inside, boolean processed) {
		if(dialog != null) {
			boolean insideDialog = dialog.getBounds().contains(info.getRelativeMouseX(), info.getRelativeMouseY());

			if(dialog.mouseClickedAnywhere(transform(info, dialog.getBounds()), button, insideDialog, processed)) {
				processed = true;
			}
			else if(!insideDialog && button == 0) {
				setDialog(null);
			}

			return processed;
		}

		if(!processed && onClickAnywhere != null && onClickAnywhere.onClick(info, button)) {
			return true;
		}

		try {
			for(Component component : subComponents) {
				if(component.shouldSkip()) {
					continue;
				}

				Rectangle bounds = getBounds(component);

				if(component.mouseClickedAnywhere(transform(info, bounds), button, inside && bounds.contains(info.getRelativeMouseX(), info.getRelativeMouseY()), processed)) {
					processed = true;
				}
			}
		}
		catch(ConcurrentModificationException error) {
		}

		if(inside && onClick != null && onClick.onClick(info, button)) {
			processed = true;
		}

		if(inside && !processed && mouseClicked(info, button)) {
			processed = true;
		}

		return processed;
	}

	/**
	 * @return <code>true</code> if the event has been processed.
	 */
	public boolean mouseClicked(ComponentRenderInfo info, int button) {
		return false;
	}

	public boolean mouseReleasedAnywhere(ComponentRenderInfo info, int button, boolean inside) {
		if(dialog != null) {
			if(dialog.mouseReleasedAnywhere(transform(info, dialog.getBounds()), button, dialog.getBounds().contains(info.getRelativeMouseX(), info.getRelativeMouseY()))) {
				return true;
			}
		}

		if(onReleaseAnywhere != null && onReleaseAnywhere.onClick(info, button)) {
			return true;
		}

		for(Component component : subComponents) {
			if(component.shouldSkip()) {
				continue;
			}

			Rectangle bounds = getBounds(component);

			if(component.mouseReleasedAnywhere(transform(info, bounds), button, bounds.contains(info.getRelativeMouseX(), info.getRelativeMouseY()))) {
				return true;
			}
		}

		if(inside && onRelease != null && onRelease.onClick(info, button)) {
			return true;
		}

		if(inside && mouseReleased(info, button)) {
			return true;
		}

		return false;
	}

	/**
	 * @return <code>true</code> if the event has been processed.
	 */
	public boolean mouseReleased(ComponentRenderInfo info, int button) {
		return false;
	}

	private boolean forEachHoveredSubComponent(ComponentRenderInfo info, BiPredicate<Component, ComponentRenderInfo> action) {
		for(Component component : subComponents) {
			Rectangle bounds = getBounds(component);

			if(!bounds.contains(info.getRelativeMouseX(), info.getRelativeMouseY())) {
				continue;
			}

			if(action.test(component, transform(info, bounds))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return <code>true</code> if the event has been processed.
	 */
	public boolean mouseScroll(ComponentRenderInfo info, int delta) {
		if(dialog != null) {
			return dialog.mouseScroll(transform(info, dialog.getBounds()), delta);
		}

		if(forEachHoveredSubComponent(info, (component, transformedInfo) -> component.mouseScroll(transformedInfo, delta))) {
			return true;
		}

		return false;
	}

	public void tick() {
		for(Component component : subComponents) {
			component.tick();
		}
	}

	public Component onClick(ClickHandler onClick) {
		this.onClick = onClick;
		return this;
	}

	public Component onClickAnwhere(ClickHandler onClickAnywhere) {
		this.onClickAnywhere = onClickAnywhere;
		return this;
	}

	public Component onRelease(ClickHandler onRelease) {
		this.onRelease = onRelease;
		return this;
	}

	public Component onReleaseAnywhere(ClickHandler onReleaseAnywhere) {
		this.onReleaseAnywhere = onReleaseAnywhere;
		return this;
	}

	public Component onKeyPressed(KeyHandler onKeyTyped) {
		onKeyPressed = onKeyTyped;
		return this;
	}

	public void setDialog(Component dialog) {
		if(this.dialog != null) {
			remove(this.dialog);
		}

		this.dialog = dialog;

		if(dialog != null) {
			add(dialog, new AlignedBoundsController(Alignment.CENTRE, Alignment.CENTRE, (component, defaultBounds) -> defaultBounds));
		}
	}

	public Component visibilityController(Controller<Boolean> visibilityController) {
		this.visibilityController = visibilityController;
		return this;
	}

}