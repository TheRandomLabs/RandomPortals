package com.therandomlabs.randomportals.client.particle;

import net.minecraft.client.particle.ParticlePortal;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;

public class ParticleRPOPortal extends ParticlePortal {
	public ParticleRPOPortal(
			World world, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed, EnumDyeColor color
	) {
		super(world, x, y, z, xSpeed, ySpeed, zSpeed);

		if (color != EnumDyeColor.PURPLE) {
			final float[] rgb = color.getColorComponentValues();

			particleRed = rgb[0];
			particleGreen = rgb[1];
			particleBlue = rgb[2];
		}
	}
}
