package io.github.solclient.client.event.impl.world;

import io.github.solclient.client.event.impl.RenderEvent;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public final class FovEvent extends RenderEvent {

	private double fov;

	public FovEvent(double fov, float tickDelta) {
		super(tickDelta);
		this.fov = fov;
	}

}