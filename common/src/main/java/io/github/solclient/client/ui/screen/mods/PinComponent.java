package io.github.solclient.client.ui.screen.mods;

import io.github.solclient.client.mod.Mod;
import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.ui.component.Component;
import io.github.solclient.client.ui.component.ComponentRenderInfo;
import io.github.solclient.client.util.data.Colour;
import io.github.solclient.client.util.data.Rectangle;

/**
 * Tiny renderer-neutral push-pin glyph used by the common mod browser.
 *
 * <p>The original UI loaded this glyph through NanoVG. Keeping the shape as
 * a few shared fill primitives means it remains identical on legacy OpenGL
 * and modern GUI extraction without adding a version-specific texture.</p>
 */
final class PinComponent extends Component {

	private final Mod mod;

	PinComponent(Mod mod) {
		this.mod = mod;
	}

	@Override
	public void render(ComponentRenderInfo info) {
		Colour colour = mod.isPinned()
				? (isHovered() ? Theme.getCurrent().fgButtonHover : Theme.getCurrent().fgButton)
				: (isHovered() ? Theme.getCurrent().fgButtonHover : Theme.getCurrent().mutedStrong);
		int value = colour.getValue();

		// 8x8 push-pin silhouette.
		DrawableHelper.fillRect(2, 0, 6, 2, value);
		DrawableHelper.fillRect(1, 2, 7, 4, value);
		DrawableHelper.fillRect(2, 4, 6, 5, value);
		DrawableHelper.fillRect(3, 5, 5, 8, value);
		DrawableHelper.fillRect(0, 7, 8, 8, value);

		super.render(info);
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return Rectangle.ofDimensions(8, 8);
	}
}
