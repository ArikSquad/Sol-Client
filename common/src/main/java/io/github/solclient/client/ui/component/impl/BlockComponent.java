package io.github.solclient.client.ui.component.impl;

import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.ui.component.ComponentRenderInfo;
import io.github.solclient.client.ui.component.controller.Controller;
import io.github.solclient.client.util.data.*;

public class BlockComponent extends ColouredComponent {

	private final Controller<Float> radius;

	public BlockComponent(Colour colour) {
		this((component, defaultColour) -> colour, (component, defaultRadius) -> 0F);
	}

	public BlockComponent(Controller<Colour> colour) {
		this(colour, (component, defaultRadius) -> 0F);
	}

	public BlockComponent(Colour colour, float radius) {
		this((component, defaultColour) -> colour, (component, defaultRadius) -> radius);
	}

	public BlockComponent(Controller<Colour> colour, float radius) {
		this(colour, (component, defaultRadius) -> radius);
	}

	public BlockComponent(Controller<Colour> colour, Controller<Float> radius) {
		super(colour);
		this.radius = radius;
	}

	@Override
	public void render(ComponentRenderInfo info) {
		Rectangle bounds = getRelativeBounds();
		float cornerRadius = Math.max(0F, radius.get(this, 0F));
		if(cornerRadius > 0F) {
			DrawableHelper.fillRoundedRect(0, 0, bounds.getWidth(), bounds.getHeight(), cornerRadius,
					getColourValue());
		}
		else {
			bounds.fill(getColour());
		}

		super.render(info);
	}

}
