package com.therandomlabs.verticalendportals.util;

import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.frame.NetherPortalFrames;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class VEPTeleporter extends Teleporter {
	public VEPTeleporter(WorldServer world) {
		super(world);
	}

	@Override
	public boolean placeInExistingPortal(Entity entity, float yaw) {
		double distance = -1.0D;

		boolean shouldCache = true;
		BlockPos pos = BlockPos.ORIGIN;

		final long chunkPos = ChunkPos.asLong(
				MathHelper.floor(entity.posX),
				MathHelper.floor(entity.posZ)
		);

		final Teleporter.PortalPosition portalPos = destinationCoordinateCache.get(chunkPos);

		if(portalPos != null) {
			distance = 0.0;
			pos = portalPos;
			portalPos.lastUpdateTime = world.getTotalWorldTime();
			shouldCache = false;
		} else {
			BlockPos pos3 = new BlockPos(entity);

			for(int xOffset = -128; xOffset <= 128; ++xOffset) {
				BlockPos pos2;

				for(int zOffset = -128; zOffset <= 128; ++zOffset) {
					BlockPos pos1 = pos3.add(
							xOffset,
							world.getActualHeight() - 1 - pos3.getY(),
							zOffset
					);

					for(; pos1.getY() >= 0; pos1 = pos2) {
						pos2 = pos1.down();

						if(BlockNetherPortal.isPortal(world, pos1)) {
							for(pos2 = pos1.down(); BlockNetherPortal.isPortal(world, pos2);
								pos2 = pos2.down()) {
								pos1 = pos2;
							}

							final double newDistance = pos1.distanceSq(pos3);

							if(distance < 0.0 || newDistance < distance) {
								distance = newDistance;
								pos = pos1;
							}
						}
					}
				}
			}
		}

		if(distance < 0.0) {
			return false;
		}

		if(shouldCache) {
			destinationCoordinateCache.put(
					chunkPos,
					new Teleporter.PortalPosition(pos, world.getTotalWorldTime())
			);
		}

		BlockPos framePos = pos.down();

		if(world.getBlockState(framePos).getBlock() != Blocks.OBSIDIAN) {
			framePos = null;

			final IBlockState portalState = world.getBlockState(pos);
			final EnumFacing.Axis axis =
					((BlockNetherPortal) portalState.getBlock()).getAxis(portalState);
			final int maxWidth = NetherPortalFrames.SIZE.apply(FrameType.fromAxis(axis)).maxWidth;

			BlockPos checkPos = pos;

			for(int offset = 1; offset < maxWidth - 1; offset++) {
				checkPos = checkPos.offset(EnumFacing.NORTH, offset);
				final Block block = world.getBlockState(checkPos).getBlock();

				if(block == Blocks.OBSIDIAN) {
					framePos = checkPos;
					break;
				}

				if(block != VEPBlocks.lateral_nether_portal) {
					break;
				}
			}
		}

		final Frame frame;

		if(framePos == null) {
			frame = null;
		} else {
			frame = NetherPortalFrames.ACTIVATED_FRAMES.detect(world, framePos);
		}

		final EnumFacing entityFacing = entity.getHorizontalFacing();

		final EnumFacing offsetDirection;
		final int offset;
		final EnumFacing forwards;
		final BlockPos bottomLeft;

		if(frame == null) {
			offsetDirection = EnumFacing.NORTH;
			offset = 0;
			forwards = EnumFacing.NORTH;
			bottomLeft = pos;
		} else if(frame.getType().isVertical()) {
			offsetDirection = frame.getWidthDirection();
			offset = frame.getWidth() / 2 + 1;
			forwards = offsetDirection.rotateY();
			bottomLeft = frame.getBottomLeft();
		} else {
			offsetDirection = EnumFacing.EAST;
			offset = frame.getWidth() / 2 + 1;
			forwards = entityFacing;
			bottomLeft = frame.getBottomLeft();
		}

		pos = bottomLeft.offset(forwards).offset(offsetDirection, offset);

		final double x = pos.getX();
		final double y = pos.getY() + 1.0;
		final double z = pos.getZ();

		float newYaw = Math.abs(entityFacing.getHorizontalIndex() * 90.0F - yaw) +
				forwards.getHorizontalIndex() * 90.0F;

		if(newYaw > 180.0F) {
			newYaw = -360.F + newYaw;
		}

		if(entity instanceof EntityPlayerMP) {
			((EntityPlayerMP) entity).connection.setPlayerLocation(
					x, y, z, newYaw, entity.rotationPitch
			);
		} else {
			entity.setLocationAndAngles(x, y, z, newYaw, entity.rotationPitch);
		}

		return true;
	}

	//My attempt to decipher the black magic that is Teleporter.makePortal
	@SuppressWarnings("Duplicates")
	@Override
	public boolean makePortal(Entity entity) {
		double distance = -1.0;

		final int x = MathHelper.floor(entity.posX);
		final int y = MathHelper.floor(entity.posY);
		final int z = MathHelper.floor(entity.posZ);

		int portalX = x;
		int portalY = y;
		int portalZ = z;

		int unknownI = 0;
		final int randomInt = random.nextInt(4);

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for(int checkX = x - 16; checkX <= x + 16; checkX++) {
			final double xDistance = checkX + 0.5 - entity.posX;

			for(int checkZ = z - 16; checkZ <= z + 16; checkZ++) {
				double zDistance = checkZ + 0.5 - entity.posZ;

				for(int checkY = world.getActualHeight() - 1; checkY >= 0; checkY--) {
					boolean shouldBreak = false;

					if(world.isAirBlock(pos.setPos(checkX, checkY, checkZ))) {
						while(checkY > 0 &&
								world.isAirBlock(pos.setPos(checkX, checkY - 1, checkZ))) {
							checkY--;
						}

						for(int unknownI2 = randomInt; unknownI2 < randomInt + 4; unknownI2++) {
							int unknownI2Modulo2 = unknownI2 % 2;
							int oneMinusUnknownI2Modulo2 = 1 - unknownI2Modulo2;

							if(unknownI2 % 4 >= 2) {
								unknownI2Modulo2 = -unknownI2Modulo2;
								oneMinusUnknownI2Modulo2 = -oneMinusUnknownI2Modulo2;
							}

							for(int i = 0; i < 3; i++) {
								for(int j = -1; j < 2; j++) {
									for(int yOffset = -1; yOffset < 4; yOffset++) {
										pos.setPos(
												checkX + j * unknownI2Modulo2 + i *
														oneMinusUnknownI2Modulo2,
												checkY + yOffset,
												checkZ + j * oneMinusUnknownI2Modulo2 - i *
														unknownI2Modulo2
										);

										final IBlockState state = world.getBlockState(pos);

										if((yOffset == -1 && !state.getMaterial().isSolid() ||
												(yOffset != -1 && !world.isAirBlock(pos)))) {
											shouldBreak = true;
											break;
										}
									}

									if(shouldBreak) {
										break;
									}
								}

								if(shouldBreak) {
									break;
								}
							}

							if(shouldBreak) {
								break;
							}

							final double yDistance = checkY + 0.5 - entity.posY;
							final double newDistance = xDistance * xDistance +
									yDistance * yDistance + zDistance * zDistance;

							if(distance < 0.0 || newDistance < distance) {
								distance = newDistance;
								portalX = checkX;
								portalY = checkY;
								portalZ = checkZ;
								unknownI = unknownI2 % 4;
							}
						}
					}
				}
			}
		}

		if(distance < 0.0) {
			for(int checkX = x - 16; checkX <= x + 16; checkX++) {
				final double xDistance = checkX + 0.5 - entity.posX;

				for(int checkZ = z - 16; checkZ <= z + 16; checkZ++) {
					final double zDistance = checkZ + 0.5 - entity.posZ;

					for(int checkY = world.getActualHeight() - 1; checkY >= 0; checkY--) {
						boolean shouldBreak = false;

						if(world.isAirBlock(pos.setPos(checkX, checkY, checkZ))) {
							while(checkY > 0 &&
									world.isAirBlock(pos.setPos(checkX, checkY - 1, checkZ))) {
								checkY--;
							}

							for(int unknownI2 = randomInt; unknownI2 < randomInt + 2; unknownI2++) {
								final int unknownI2Modulo2 = unknownI2 % 2;
								final int oneMinusUnknownI2Modulo2 = 1 - unknownI2Modulo2;

								for(int i = -1; i < 3; ++i) {
									for(int yOffset = -1; yOffset < 4; yOffset++) {
										pos.setPos(
												checkX + i * unknownI2Modulo2,
												checkY + yOffset,
												checkZ + i * oneMinusUnknownI2Modulo2
										);

										final IBlockState state = world.getBlockState(pos);

										if((yOffset == -1 && !state.getMaterial().isSolid() ||
												(yOffset != -1 && !world.isAirBlock(pos)))) {
											shouldBreak = true;
											break;
										}
									}

									if(shouldBreak) {
										break;
									}
								}

								if(shouldBreak) {
									break;
								}

								final double yDistance = checkY + 0.5 - entity.posY;
								final double newDistance = xDistance * xDistance +
										yDistance * yDistance + zDistance * zDistance;

								if(distance < 0.0 || newDistance < distance) {
									distance = newDistance;
									portalX = checkX;
									portalY = checkY;
									portalZ = checkZ;
									unknownI = unknownI2 % 2;
								}
							}
						}
					}
				}
			}
		}

		int xMultiplier = unknownI % 2;
		int zMultiplier = 1 - xMultiplier;

		if(unknownI % 4 >= 2) {
			xMultiplier = -xMultiplier;
			zMultiplier = -zMultiplier;
		}

		final IBlockState air = Blocks.AIR.getDefaultState();
		final IBlockState obsidian = Blocks.OBSIDIAN.getDefaultState();

		if(distance < 0.0) {
			portalY = MathHelper.clamp(portalY, 70, world.getActualHeight() - 10);

			for(int i = -1; i <= 1; i++) {
				for(int j = 0; j < 2; j++) {
					for(int yOffset = -1; yOffset < 3; yOffset++) {
						world.setBlockState(new BlockPos(
								portalX + j * xMultiplier + i * zMultiplier,
								portalY + yOffset,
								portalZ + j * zMultiplier - i * xMultiplier
						), yOffset == -1 ? obsidian : air);
					}
				}
			}
		}

		final IBlockState portal = VEPBlocks.vertical_nether_portal.getDefaultState().
				withProperty(
						BlockPortal.AXIS,
						xMultiplier == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X
				).
				withProperty(BlockNetherPortal.USER_PLACED, false);

		for(int horzOffset = -1; horzOffset < 3; horzOffset++) {
			for(int yOffset = -1; yOffset < 4; yOffset++) {
				final boolean frame =
						horzOffset == -1 || horzOffset == 2 || yOffset == -1 || yOffset == 3;

				if(frame) {
					world.setBlockState(new BlockPos(
							portalX + horzOffset * xMultiplier,
							portalY + yOffset,
							portalZ + horzOffset * zMultiplier
					), obsidian, 2);
				}
			}
		}

		for(int horzOffset = -1; horzOffset < 3; horzOffset++) {
			for(int yOffset = -1; yOffset < 4; yOffset++) {
				final boolean frame =
						horzOffset == -1 || horzOffset == 2 || yOffset == -1 || yOffset == 3;

				if(!frame) {
					world.setBlockState(new BlockPos(
							portalX + horzOffset * xMultiplier,
							portalY + yOffset,
							portalZ + horzOffset * zMultiplier
					), portal, 2);
				}
			}
		}

		return true;
	}
}
