package io.github.solclient.client.ui.screen.mods;

import java.util.function.BooleanSupplier;

import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.ui.component.Component;
import io.github.solclient.client.ui.component.ComponentRenderInfo;
import io.github.solclient.client.util.data.Colour;
import io.github.solclient.client.util.data.Rectangle;

final class ChevronComponent extends Component {

	private final BooleanSupplier expanded;

	ChevronComponent(BooleanSupplier expanded) {
		this.expanded = expanded;
	}

	@Override
	public void render(ComponentRenderInfo info) {
		Colour colour = isHovered() ? Theme.getCurrent().fgButtonHover : Theme.getCurrent().fgButton;
		int value = colour.getValue();

		if(expanded.getAsBoolean()) {
			DrawableHelper.fillRect(1, 2, 3, 4, value);
			DrawableHelper.fillRect(2, 3, 4, 5, value);
			DrawableHelper.fillRect(3, 4, 5, 6, value);
			DrawableHelper.fillRect(4, 3, 6, 5, value);
			DrawableHelper.fillRect(5, 2, 7, 4, value);
		}
		else {
			DrawableHelper.fillRect(2, 1, 4, 3, value);
			DrawableHelper.fillRect(3, 2, 5, 4, value);
			DrawableHelper.fillRect(4, 3, 6, 5, value);
			DrawableHelper.fillRect(5, 4, 7, 6, value);
		}

		super.render(info);
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return Rectangle.ofDimensions(8, 8);
	}
}
