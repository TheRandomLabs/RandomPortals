package com.therandomlabs.verticalendportals.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public final class FrameBlock {
	private static final IForgeRegistry<Block> BLOCK_REGISTRY =
			GameRegistry.findRegistry(Block.class);

	public String registryName;
	public int meta = OreDictionary.WILDCARD_VALUE;
	public int minimumAmount;

	private transient boolean blockRetrieved;
	private transient Block block;
	private transient FrameBlock[] blocks;

	public FrameBlock() {}

	public FrameBlock(Block block, int minimumAmount) {
		this(block, OreDictionary.WILDCARD_VALUE, minimumAmount);
	}

	public FrameBlock(Block block, int meta, int minimumAmount) {
		registryName = block.getRegistryName().toString();
		this.meta = meta;
		this.minimumAmount = minimumAmount;
	}

	@Override
	public String toString() {
		return "FrameBlock[registryName=" + registryName + ",minimumAmount=" + minimumAmount + "]";
	}

	public Block getBlock() {
		if(!blockRetrieved) {
			block = BLOCK_REGISTRY.getValue(new ResourceLocation(registryName));

			if(block == Blocks.AIR) {
				block = null;
			}

			blockRetrieved = true;
		}

		return block;
	}

	public FrameBlock getActualBlock() {
		return getBlocks().length == 0 ? null : blocks[0];
	}

	private FrameBlock[] getBlocks() {
		if(blocks != null) {
			return blocks;
		}

		if(!registryName.startsWith("ore:")) {
			if(getBlock() == null) {
				blocks = new FrameBlock[0];
				return blocks;
			}

			blocks = new FrameBlock[] {
					this
			};
			return blocks;
		}

		final List<ItemStack> ores = OreDictionary.getOres(registryName.substring(4));

		if(ores.isEmpty()) {
			blocks = new FrameBlock[0];
			return blocks;
		}

		final List<FrameBlock> blocks = new ArrayList<>(ores.size());

		for(ItemStack ore : ores) {
			final Item item = ore.getItem();

			if(item instanceof ItemBlock) {
				blocks.add(new FrameBlock(((ItemBlock) item).getBlock(), ore.getMetadata()));
			}
		}

		this.blocks = blocks.toArray(new FrameBlock[0]);
		return this.blocks;
	}

	public boolean isValid() {
		return getBlocks() != null;
	}

	public void ensureCorrect() {
		if(minimumAmount < 0) {
			minimumAmount = 0;
		}
	}

	public boolean test(IBlockState state) {
		final Block block = state.getBlock();
		final int meta = block.getMetaFromState(state);

		for(FrameBlock frameBlock : blocks) {
			if(frameBlock.getBlock() == block && (frameBlock.meta == OreDictionary.WILDCARD_VALUE ||
					frameBlock.meta == meta)) {
				return true;
			}
		}

		return false;
	}
}
