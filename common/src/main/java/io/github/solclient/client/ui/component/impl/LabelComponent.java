package io.github.solclient.client.ui.component.impl;

import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.ui.component.ComponentRenderInfo;
import io.github.solclient.client.ui.component.controller.Controller;
import io.github.solclient.client.util.data.*;

public final class LabelComponent extends ColouredComponent {

	private final Controller<String> text;
	private float scale = 1F;

	public LabelComponent(String text) {
		this((component, defaultText) -> I18n.translate(text), (component, defaultColour) -> defaultColour);
	}

	public LabelComponent(Controller<String> text) {
		this(text, (component, defaultColour) -> defaultColour);
	}

	public LabelComponent(Controller<String> text, Controller<Colour> colour) {
		super(colour);
		this.text = text;
	}

	@Override
	public void render(ComponentRenderInfo info) {
		if(scale != 1F) {
			io.github.solclient.client.platform.mc.render.GlStateManager.pushMatrix();
			io.github.solclient.client.platform.mc.render.GlStateManager.scale(scale, scale, 1);
		}

		font.render(getText(), 0, 0, getColourValue());

		if(scale != 1F) {
			io.github.solclient.client.platform.mc.render.GlStateManager.popMatrix();
		}

		super.render(info);
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return new Rectangle(0, 0, (int) (font.getTextWidth(getText()) * scale),
				(int) (font.getHeight() * scale + 2 * scale));
	}

	public LabelComponent scaled(float scale) {
		this.scale = Math.max(0.1F, scale);
		return this;
	}

	public String getText() {
		return text.get(this, "");
	}

}
