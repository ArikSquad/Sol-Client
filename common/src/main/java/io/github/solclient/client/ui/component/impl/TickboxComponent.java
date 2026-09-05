package io.github.solclient.client.ui.component.impl;

import java.util.function.Consumer;

import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.ui.component.Component;
import io.github.solclient.client.ui.component.ComponentRenderInfo;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.Colour;
import io.github.solclient.client.util.data.Rectangle;

/**
 * The common boolean control. It is named Tickbox for compatibility with the
 * original option editor, but deliberately follows the redesigned pill switch
 * used by the develop UI rather than loading the old square texture.
 */
public final class TickboxComponent extends Component {

	private boolean value;
	private final Consumer<Boolean> booleanConsumer;
	private final Component hoverController;

	public TickboxComponent(boolean value, Consumer<Boolean> booleanConsumer, Component hoverController) {
		this.value = value;
		this.booleanConsumer = booleanConsumer;
		this.hoverController = hoverController;

		if(hoverController != null) {
			hoverController.onClick((info, button) -> {
				if(!super.isHovered() && button == 0) {
					mouseClicked(info, button);
				}
				return true;
			});
		}
	}

	@Override
	public boolean isHovered() {
		return hoverController == null ? super.isHovered() : hoverController.isHovered();
	}

	@Override
	public void render(ComponentRenderInfo info) {
		boolean hovered = isHovered();
		Colour track = value
				? (hovered ? Theme.getCurrent().accentHover : Theme.getCurrent().accent)
				: (hovered ? Theme.getCurrent().buttonHover : Theme.getCurrent().button);
		DrawableHelper.fillRoundedRect(0, 0, 24, 11, 5.5F, track.getValue());

		int centreX = value ? 18 : 5;
		Colour handle = value ? Theme.getCurrent().accentFg : Theme.getCurrent().fg;
		int colour = handle.getValue();
		for(int row = -4; row <= 4; row++) {
			int halfWidth = (int) Math.sqrt(16 - row * row);
			DrawableHelper.fillRect(centreX - halfWidth, 5 + row,
					centreX + halfWidth + 1, 6 + row, colour);
		}

		super.render(info);
	}

	@Override
	public boolean mouseClicked(ComponentRenderInfo info, int button) {
		if(button != 0) {
			return false;
		}

		Utils.playClickSound(true);
		value = !value;
		booleanConsumer.accept(value);
		return true;
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return Rectangle.ofDimensions(24, 11);
	}
}
