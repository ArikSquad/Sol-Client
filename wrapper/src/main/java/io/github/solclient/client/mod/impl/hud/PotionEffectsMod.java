package io.github.solclient.client.mod.impl.hud;

import java.util.*;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.mod.annotation.*;
import io.github.solclient.client.mod.hud.*;
import io.github.solclient.client.platform.mc.DrawableHelper;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.platform.mc.render.GlStateManager;
import io.github.solclient.client.platform.mc.texture.Texture;
import io.github.solclient.client.platform.mc.world.entity.effect.*;
import io.github.solclient.client.util.data.*;

public final class PotionEffectsMod extends HudMod {

	public static final PotionEffectsMod INSTANCE = new PotionEffectsMod();

	private static final StatusEffect SPEED = StatusEffect.create(StatusEffectType.SPEED);
	private static final StatusEffect STRENGTH = StatusEffect.create(StatusEffectType.STRENGTH);

	@Expose
	@Option
	private VerticalAlignment alignment = VerticalAlignment.MIDDLE;
	@Expose
	@Option
	private boolean icon = true;
	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY)
	private boolean background = false;
	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY)
	private boolean shadow = true;
	@Expose
	@Option
	private boolean title = true;
	@Expose
	@Option(applyToAllClass = Option.TEXT_COLOUR_CLASS)
	private Colour titleColour = Colour.WHITE;
	@Expose
	@Option
	private boolean duration = true;
	@Expose
	@Option
	private Colour durationColour = new Colour(8355711);
	@Expose
	@Option
	@Slider(min = 2, max = 25, step = 1)
	private float spacing = 15;

	@Override
	public String getId() {
		return "potion_effects";
	}

	@Override
	public Rectangle getBounds(Position position) {
		int y = position.getY();

		switch(alignment) {
			case TOP:
				break;
			case MIDDLE:
				y -= getHeight(2) / 2 * getScale();
				break;
			case BOTTOM:
				y -= getHeight(2) * getScale();
				break;
		}

		return new Rectangle(position.getX(), y, getWidth(), getHeight(2) + 12 + (background ? 2 : 0));
	}

	private int getHeight(int size) {
		return (int) (getEffectHeight() * size - spacing);
	}

	private int getEffectHeight() {
		return (int) (18 + spacing);
	}

	@Override
	public void render(Position position, boolean editMode) {
		int x = position.getX();
		int y = position.getY();
		Collection<StatusEffect> effects;

		GlStateManager.enableBlend();

		if(editMode || !mc.hasPlayer()) {
			effects = Arrays.asList(SPEED, STRENGTH);
		}
		else {
			effects = mc.getPlayer().getEntityStatusEffects();
		}

		switch(alignment) {
			case TOP:
				break;
			case MIDDLE:
				y -= (getHeight(effects.size()) / 2);
				break;
			case BOTTOM:
				y -= getHeight(effects.size());
		}

		if(!effects.isEmpty()) {
			for(StatusEffect effect : effects) {
				GlStateManager.resetColour();
				StatusEffectType type = effect.getType();

				int width = getWidth();
				int iconX = x + 6;
				int textX = x + 28;

				if(!title && !duration) {
					iconX++;
				}

				if(!icon) {
					textX -= 18;
				}

				if(background) {
					mc.getTextureManager().bind(Texture.INVENTORY_ID);
					int halfWidth = width / 2;
					DrawableHelper.fillTexturedRect(x, y, 0, 166, halfWidth, 32, 256, 256);
					DrawableHelper.fillTexturedRect(x + width / 2, y, 120 - halfWidth, 166, halfWidth, 32, 256, 256);
				}

				int centreText = y + 12;

				if(icon) {
					mc.getTextureManager().bind(Texture.MOB_EFFECTS_ATLAS_ID);
					type.render(iconX, y + 7);
				}

				if(title) {
					String titleText = I18n.translate(type.getName());

					if(effect.showAmplifier()) {
						titleText += " " + I18n.translate(effect.getAmplifierName());
					}

					font.render(titleText, textX, duration ? y + 7 : centreText, titleColour.getValue(), shadow);
				}

				if(duration) {
					String duration = effect.getDurationText();
					font.render(duration, textX, title ? y + 17 : centreText, durationColour.getValue(), shadow);
				}

				y += getEffectHeight();
			}
		}
	}

	private int getWidth() {
		int base = 0;

		if(!icon) {
			base = -18;
		}

		if(!title) {
			if(!duration) {
				return base + 32;
			}

			return base + 56;
		}

		return base + 140;
	}

}