package com.therandomlabs.verticalendportals.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BlockPattern {
	public static class PatternHelper {
		private final BlockPos frontTopLeft;
		private final EnumFacing forwards;
		private final EnumFacing up;
		private final LoadingCache<BlockPos, BlockWorldState> cache;
		private final int width;
		private final int height;
		private final ImmutableList<BlockPos> positions;

		public PatternHelper(BlockPos frontTopLeft, EnumFacing forwards, EnumFacing up,
				LoadingCache<BlockPos, BlockWorldState> cache, int width, int height,
				List<BlockPos> positions) {
			this.frontTopLeft = frontTopLeft;
			this.forwards = forwards;
			this.up = up;
			this.cache = cache;
			this.width = width;
			this.height = height;
			this.positions = ImmutableList.copyOf(positions);
		}

		public String toString() {
			return MoreObjects.toStringHelper(this).
					add("up", up).
					add("forwards", forwards).
					add("frontTopLeft", frontTopLeft).
					toString();
		}

		public BlockPos getFrontTopLeft() {
			return frontTopLeft;
		}

		public EnumFacing getForwards() {
			return forwards;
		}

		public EnumFacing getUp() {
			return up;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public BlockWorldState translateOffset(int palmOffset, int thumbOffset, int fingerOffset) {
			return cache.getUnchecked(BlockPattern.translateOffset(
					frontTopLeft, getForwards(), getUp(), palmOffset, thumbOffset, fingerOffset
			));
		}

		public ImmutableList<BlockPos> getPositions() {
			return positions;
		}
	}

	static class BPCacheLoader extends CacheLoader<BlockPos, BlockWorldState> {
		private final World world;
		private final boolean forceLoad;

		public BPCacheLoader(World world, boolean forceLoad) {
			this.world = world;
			this.forceLoad = forceLoad;
		}

		public BlockWorldState load(BlockPos pos) throws Exception {
			return new BlockWorldState(world, pos, forceLoad);
		}
	}

	private final Predicate<BlockWorldState>[][][] blockMatches;
	private final int fingerLength;
	private final int thumbLength;
	private final int palmLength;

	public BlockPattern(Predicate<BlockWorldState>[][][] predicates) {
		blockMatches = predicates;
		fingerLength = predicates.length;

		if(fingerLength > 0) {
			thumbLength = predicates[0].length;

			if(thumbLength > 0) {
				palmLength = predicates[0][0].length;
			} else {
				palmLength = 0;
			}
		} else {
			thumbLength = 0;
			palmLength = 0;
		}
	}

	public int getFingerLength() {
		return fingerLength;
	}

	public int getThumbLength() {
		return thumbLength;
	}

	public int getPalmLength() {
		return palmLength;
	}

	public BlockPattern.PatternHelper match(World worldIn, BlockPos pos) {
		final LoadingCache<BlockPos, BlockWorldState> cache = createLoadingCache(worldIn, false);
		final int i = Math.max(Math.max(palmLength, thumbLength), fingerLength);

		for(BlockPos pos2 : BlockPos.getAllInBox(pos, pos.add(i - 1, i - 1, i - 1))) {
			for(EnumFacing facing : EnumFacing.values()) {
				for(EnumFacing facing2 : EnumFacing.values()) {
					if(facing2 != facing && facing2 != facing.getOpposite()) {
						final BlockPattern.PatternHelper patternHelper =
								checkPatternAt(pos2, facing, facing2, cache);

						if(patternHelper != null) {
							return patternHelper;
						}
					}
				}
			}
		}

		return null;
	}

	private BlockPattern.PatternHelper checkPatternAt(BlockPos pos, EnumFacing finger,
			EnumFacing thumb, LoadingCache<BlockPos, BlockWorldState> cache) {
		final int total = (palmLength + 1) * (fingerLength + 1);
		final List<BlockPos> positions = new ArrayList<>(total);

		for(int i = 0; i < palmLength; ++i) {
			for(int j = 0; j < thumbLength; ++j) {
				for(int k = 0; k < fingerLength; ++k) {
					final BlockPos translated = translateOffset(pos, finger, thumb, i, j, k);

					positions.add(translated);

					if(!blockMatches[k][j][i].test(cache.getUnchecked(translated))) {
						return null;
					}
				}
			}
		}

		return new BlockPattern.PatternHelper(
				pos, finger, thumb, cache, palmLength, thumbLength, positions
		);
	}

	public static LoadingCache<BlockPos, BlockWorldState> createLoadingCache(World world,
			boolean forceLoad) {
		return CacheBuilder.newBuilder().build(new BPCacheLoader(world, forceLoad));
	}

	protected static BlockPos translateOffset(BlockPos pos, EnumFacing finger, EnumFacing thumb,
			int palmOffset, int thumbOffset, int fingerOffset) {
		if(finger == thumb || finger == thumb.getOpposite()) {
			throw new IllegalArgumentException("Invalid forwards and up combination");
		}

		final Vec3i vec3i = new Vec3i(
				finger.getXOffset(),
				finger.getYOffset(),
				finger.getZOffset()
		);

		final Vec3i vec3i1 = new Vec3i(
				thumb.getXOffset(),
				thumb.getYOffset(),
				thumb.getZOffset()
		);

		final Vec3i vec3i2 = vec3i.crossProduct(vec3i1);

		return pos.add(
				vec3i1.getX() * -thumbOffset + vec3i2.getX() * palmOffset +
						vec3i.getX() * fingerOffset,
				vec3i1.getY() * -thumbOffset + vec3i2.getY() * palmOffset +
						vec3i.getY() * fingerOffset,
				vec3i1.getZ() * -thumbOffset + vec3i2.getZ() * palmOffset +
						vec3i.getZ() * fingerOffset
		);
	}
}