package io.github.solclient.client.ui.component.impl;

import java.util.function.Consumer;

import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.ui.component.*;
import io.github.solclient.client.ui.component.controller.*;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.*;

public final class SliderComponent extends Component {

	private float min;
	private float max;
	private float step;
	private float value;
	private final Consumer<Float> callback;
	private boolean selected;
	private final Controller<Colour> colour = new AnimatedColourController(
			(component, defaultColour) -> component.isHovered() || selected
					? Theme.getCurrent().accentHover : Theme.getCurrent().accent);
	private final Component hoverController;

	public SliderComponent(float min, float max, float step, float value, Consumer<Float> callback, Component hoverController) {
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = value;
		this.callback = callback;
		this.hoverController = hoverController;

		hoverController.onClick((info, button) -> {
			if(!super.isHovered()) {
				mouseClicked(info, button);
				return true;
			}

			return false;
		});
	}

	@Override
	public boolean isHovered() {
		return hoverController.isHovered();
	}

	@Override
	public void render(ComponentRenderInfo info) {
		int x = (int) (100 * (((value - min) / (max - min)))) - 5;

		// Keep the slider a renderer primitive rather than a version-specific
		// texture. This is the same pill geometry on legacy immediate mode and
		// modern GuiGraphics.
		DrawableHelper.fillRoundedRect(0, 3, 100, 7, 2, Theme.getCurrent().buttonSecondary.getValue());
		DrawableHelper.fillRoundedRect(x, 0, x + 10, 10, 5, colour.get(this, null).getValue());

		if(selected) {
			value = Utils.clamp((float) (min + Math.floor(((info.getRelativeMouseX() / 100F) * (max - min)) / step) * step), min, max);
			callback.accept(value);
		}

		super.render(info);
	}

	@Override
	public boolean mouseClicked(ComponentRenderInfo info, int button) {
		if(button == 0) {
			Utils.playClickSound(true);
			selected = true;
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseReleasedAnywhere(ComponentRenderInfo info, int button, boolean inside) {
		if(selected) {
			selected = false;
			return true;
		}

		return false;
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return new Rectangle(0, 0, 100, 10);
	}

}
