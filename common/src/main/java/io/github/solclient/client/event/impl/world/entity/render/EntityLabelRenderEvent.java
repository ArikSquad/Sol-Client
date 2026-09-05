package io.github.solclient.client.event.impl.world.entity.render;

import io.github.solclient.client.event.Cancellable;
import io.github.solclient.client.event.impl.RenderEvent;
import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.world.entity.Entity;
import lombok.*;

/**
 * Allows common features to add or replace the label rendered above an entity.
 * The native adapter turns the result into that version's render-state/text
 * object after all common handlers have run.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class EntityLabelRenderEvent extends RenderEvent implements Cancellable {

	private final Entity entity;
	private Text text;
	private boolean cancelled;

	public EntityLabelRenderEvent(Entity entity, float tickDelta) {
		super(tickDelta);
		this.entity = entity;
	}
}
