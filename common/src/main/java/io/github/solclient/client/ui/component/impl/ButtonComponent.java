package io.github.solclient.client.ui.component.impl;

import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.ui.component.ComponentRenderInfo;
import io.github.solclient.client.ui.component.controller.*;
import io.github.solclient.client.ui.component.handler.ClickHandler;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.*;
import lombok.Getter;

public final class ButtonComponent extends ColouredComponent {

	@Getter
	private final Controller<String> text;
	private final Controller<Colour> foreground;
	private ScaledIconComponent icon;
	private ButtonType type = ButtonType.NORMAL;
	private int width = type.getWidth();
	private int height = 20;

	public ButtonComponent(String text, Controller<Colour> colour) {
		this(text, colour, (component, defaultColour) -> colour.get(component, defaultColour).getOptimalForeground());
	}

	public ButtonComponent(Controller<String> text, Controller<Colour> colour) {
		this(text, colour, (component, defaultColour) -> colour.get(component, defaultColour).getOptimalForeground());
	}

	public ButtonComponent(String text, Controller<Colour> colour, Controller<Colour> foreground) {
		this((component, defaultText) -> I18n.translate(text), colour, foreground);
	}

	public ButtonComponent(Controller<String> text, Controller<Colour> colour, Controller<Colour> foreground) {
		super(colour);
		this.text = text;
		this.foreground = foreground;

		add(new LabelComponent(text, foreground), (component, defaultBounds) -> {
			int x = 0;
			int contentWidth = getBounds().getWidth();

			if(icon != null && getBounds().getWidth() < icon.getBounds().getEndX()
					+ defaultBounds.getWidth() + 16) {
				x = icon.getBounds().getEndX() + 1;
				contentWidth -= x + 7;
			}

			return defaultBounds.offset(x + (contentWidth / 2 - defaultBounds.getWidth() / 2),
					getBounds().getHeight() / 2 - defaultBounds.getHeight() / 2);
		});
	}

	@Override
	public void render(ComponentRenderInfo info) {
		Colour colour = getColour();
		DrawableHelper.fillRoundedRect(0, 0, width, height, height / 2.0F, colour.getValue());

		super.render(info);
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return new Rectangle(0, 0, width, height);
	}

	@Override
	public ButtonComponent onClick(ClickHandler onClick) {
		super.onClick(onClick);
		return this;
	}

	public ButtonComponent withIcon(String name) {
		icon = new ScaledIconComponent(name, 12, 12, foreground);
		add(icon, new AlignedBoundsController(Alignment.START, Alignment.CENTRE,
				(component, defaultBounds) -> new Rectangle(defaultBounds.getY(), defaultBounds.getY(),
						defaultBounds.getWidth(), defaultBounds.getHeight())));
		return this;
	}

	public ButtonComponent type(ButtonType type) {
		this.type = type;
		this.width = type.getWidth();
		return this;
	}

	public ButtonComponent width(int width) {
		this.width = Math.max(1, width);
		return this;
	}

	public ButtonComponent height(int height) {
		this.height = Math.max(1, height);
		return this;
	}

	public static ButtonComponent done(Runnable onClick) {
		return new ButtonComponent("gui.done", Theme.accent(), Theme.accentFg())
						.onClick((info, button) -> {
							if(button == 0) {
								Utils.playClickSound(true);
								onClick.run();

								return true;
							}
							return false;
						}).withIcon("sol_client_tick");
	}

}
