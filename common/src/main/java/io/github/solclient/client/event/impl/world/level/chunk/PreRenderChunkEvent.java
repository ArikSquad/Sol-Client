package io.github.solclient.client.event.impl.world.level.chunk;

import io.github.solclient.client.platform.mc.world.level.chunk.CompiledChunk;
import lombok.*;

@Data
@RequiredArgsConstructor
public final class PreRenderChunkEvent {

	private final CompiledChunk chunk;
	/**
	 * Additional camera-space Y translation requested by a common feature.
	 *
	 * <p>The old renderer exposed a mutable GL matrix at this point. Modern
	 * renderers build immutable chunk draw data instead, so the event carries
	 * the intent and the version adapter applies it to its native draw state.</p>
	 */
	@Setter
	private float offsetY;

}
