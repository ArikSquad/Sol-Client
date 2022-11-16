package io.github.solclient.client.mod.impl.hud.crosshair;

import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.event.EventHandler;
import io.github.solclient.client.event.impl.hud.PreHudElementRenderEvent;
import io.github.solclient.client.mod.annotation.Option;
import io.github.solclient.client.mod.hud.HudMod;
import io.github.solclient.client.platform.mc.*;
import io.github.solclient.client.platform.mc.raycast.HitType;
import io.github.solclient.client.platform.mc.render.GlStateManager;
import io.github.solclient.client.platform.mc.resource.Identifier;
import io.github.solclient.client.platform.mc.texture.Texture;
import io.github.solclient.client.platform.mc.world.entity.player.GameMode;
import io.github.solclient.client.util.*;
import io.github.solclient.client.util.data.Colour;

public final class CrosshairMod extends HudMod {

	public static final CrosshairMod INSTANCE = new CrosshairMod();
	private static final Identifier CROSSHAIRS = Identifier.solClient("crosshairs.png");

	@Expose
	@Option
	private CrosshairStyle style = CrosshairStyle.DEFAULT;
	@Expose
	@Option
	private boolean thirdPerson = true;
	@Expose
	@Option
	private boolean spectatorAlways = false;
	@Expose
	@Option
	private boolean debug = false;
	@Expose
	@Option
	private boolean blending = true;
	@Expose
	@Option
	private Colour crosshairColour = Colour.WHITE;
	@Expose
	@Option
	private boolean highlightEntities = false;
	@Expose
	@Option
	private Colour entityColour = Colour.PURE_RED;

	@Override
	public String getId() {
		return "crosshair";
	}

	@EventHandler
	public void onCrosshairRender(PreHudElementRenderEvent event) {
		if(event.getElement() == VanillaHudElement.CROSSHAIR) {
			event.cancel();
			if((!debug && mc.getOptions().debugOverlay())
					|| (!spectatorAlways && (mc.getPlayerState().getGameMode() == GameMode.SPECTATOR
							&& mc.getHitResult().getType() != HitType.ENTITY))
					|| (!thirdPerson && mc.getOptions().perspective().isThirdPerson())) {
				return;
			}
			Window window = mc.getWindow();

			try(ScopeGuard _p = GlScopeGuards.push()) {
				GlStateManager.scale(getScale(), getScale(), getScale());
				GlStateManager.translate(window.scaledWidth() / getScale() / 2 - 7,
						window.scaledHeight() / getScale() / 2 - 7, 0);

				crosshairColour.bind();

				GlStateManager.enableBlend();
				GlStateManager.blendFunction(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

				if(highlightEntities && mc.getHitResult().getType() == HitType.ENTITY
						&& mc.getHitResult().getEntity() != null && !(mc.getHitResult().getEntity().isGloballyInvisible()
								|| mc.getHitResult().getEntity().isInvisibleTo(mc.getPlayer()))) {
					entityColour.bind();
				}
				else if(blending) {
					GlStateManager.blendFunction(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_ONE,
							GL11.GL_ZERO);
				}

				if(style == CrosshairStyle.DEFAULT) {
					mc.getTextureManager().bind(Texture.ICONS_ID);
					DrawableHelper.fillTexturedRect(0, 0, 0, 0, 16, 16, 16, 16);
				}
				else {
					mc.getTextureManager().bind(CROSSHAIRS);
					int v = (style.ordinal() - 2) * 16;
					DrawableHelper.fillTexturedRect(0, 0, 0, v, 16, 16, 16, 16);
					mc.getTextureManager().bind(Texture.ICONS_ID);
				}

				GlStateManager.resetColour();
			}
		}
	}

}