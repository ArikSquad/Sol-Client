package io.github.solclient.client.mod.impl;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

import io.github.solclient.client.event.EventHandler;
import io.github.solclient.client.event.impl.shader.PostProcessingEvent;
import io.github.solclient.client.mod.*;
import io.github.solclient.client.mod.annotation.*;
import io.github.solclient.client.platform.mc.Window;
import io.github.solclient.client.platform.mc.shader.*;

public final class ColourSaturationMod extends Mod implements PrimaryIntegerSettingMod {

	public static final ColourSaturationMod INSTANCE = new ColourSaturationMod();

	@Expose
	@Option
	@Slider(min = 0, max = 2F, step = 0.1F)
	private float saturation = 1f;
	private ShaderChain chain;
	private float uniformSaturation;

	@Override
	public String getId() {
		return "colour_saturation";
	}

	@Override
	public ModCategory getCategory() {
		return ModCategory.VISUAL;
	}

	private void update() {
		if(chain == null) {
			uniformSaturation = saturation;
			try {
				chain = ShaderChain.create("{" +
						"    \"targets\": [" +
						"        \"swap\"," +
						"        \"previous\"" +
						"    ]," +
						"    \"passes\": [" +
						"        {" +
						"            \"name\": \"color_convolve\"," +
						"            \"intarget\": \"minecraft:main\"," +
						"            \"outtarget\": \"swap\"," +
						"            \"auxtargets\": [" +
						"                {" +
						"                    \"name\": \"PrevSampler\"," +
						"                    \"id\": \"previous\"" +
						"                }" +
						"            ]," +
						"            \"uniforms\": [" +
						"                {" +
						"                    \"name\": \"Saturation\"," +
						"                    \"values\": [ %s ]" +
						"                }" +
						"            ]" +
						"        }," +
						"        {" +
						"            \"name\": \"blit\"," +
						"            \"intarget\": \"swap\"," +
						"            \"outtarget\": \"previous\"" +
						"        }," +
						"        {" +
						"            \"name\": \"blit\"," +
						"            \"intarget\": \"swap\"," +
						"            \"outtarget\": \"minecraft:main\"" +
						"        }" +
						"    ]" +
						"}");
				chain.updateWindowSize(Window.displayWidth(), Window.displayHeight());
			}
			catch(JsonSyntaxException | IOException error) {
				logger.error("Could not load saturation shader", error);
			}
		}

		if(uniformSaturation != saturation) {
			chain.getShaders().forEach((shader) -> {
				ShaderUniform saturationUniform = shader.getUniform("Saturation");
				if(saturationUniform != null) {
					saturationUniform.set(saturation);
				}
			});
			uniformSaturation = saturation;
		}
	}

	@EventHandler
	public void onPostProcessing(PostProcessingEvent event) {
		update();
		event.getShaders().add(chain);
	}

	@Override
	protected void onEnable() {
		super.onEnable();
		chain = null;
	}

	@Override
	public void decrement() {
		saturation = Math.max(0, saturation - 0.1F);
	}

	@Override
	public void increment() {
		saturation = Math.min(2, saturation + 0.1F);
	}

}