package com.therandomlabs.verticalendportals.util;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class VEPUtils {
	public static final IForgeRegistry<Block> BLOCK_REGISTRY =
			GameRegistry.findRegistry(Block.class);

	private VEPUtils() {}

	public static Block getBlock(String blockName, Block defaultBlock) {
		final Block block = BLOCK_REGISTRY.getValue(new ResourceLocation(blockName));
		return block == null ? defaultBlock : block;
	}
}
