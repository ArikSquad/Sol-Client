package io.github.solclient.client.platform.mc.world.particle;

import io.github.solclient.client.platform.basic.BasicPlatform;

public interface ParticleType {

	ParticleType CRIT = BasicPlatform.particleType("CRIT");
	ParticleType MAGIC = BasicPlatform.particleType("MAGIC");
	ParticleType SNOWBALL = BasicPlatform.particleType("SNOWBALL");
	ParticleType SLIME = BasicPlatform.particleType("SLIME");
	ParticleType FLAME = BasicPlatform.particleType("FLAME");

}
