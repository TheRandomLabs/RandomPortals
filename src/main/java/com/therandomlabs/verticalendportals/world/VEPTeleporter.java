package com.therandomlabs.verticalendportals.world;

import com.therandomlabs.randompatches.util.RPTeleporter;
import com.therandomlabs.verticalendportals.api.config.FrameBlock;
import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.netherportal.NetherPortal;
import com.therandomlabs.verticalendportals.api.netherportal.NetherPortalActivator;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import com.therandomlabs.verticalendportals.frame.NetherPortalFrames;
import com.therandomlabs.verticalendportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.verticalendportals.world.storage.NetherPortalSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class VEPTeleporter extends Teleporter {
	public VEPTeleporter(WorldServer world) {
		super(world);
	}

	@Override
	public void placeInPortal(Entity entity, float yaw) {
		final boolean placePortal;

		if(world.provider.getDimension() == DimensionType.THE_END.getId()) {
			final NetherPortalType portalType = NetherPortalTeleportHandler.getPortalType(entity);
			placePortal = portalType != null && portalType.forcePortal;
		} else {
			placePortal = true;
		}

		if(placePortal) {
			if(!placeInExistingPortal(entity, yaw)) {
				makePortal(entity);
				placeInExistingPortal(entity, yaw);
			}

			return;
		}

		final IBlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
		final IBlockState air = Blocks.AIR.getDefaultState();

		final int x = MathHelper.floor(entity.posX);
		final int y = MathHelper.floor(entity.posY) - 1;
		final int z = MathHelper.floor(entity.posZ);

		for(int zOffset = -2; zOffset < 3; zOffset++) {
			for(int xOffset = -2; xOffset < 3; xOffset++) {
				for(int yOffset = -1; yOffset < 3; yOffset++) {
					world.setBlockState(
							new BlockPos(
									x + xOffset,
									y + yOffset,
									z - zOffset
							),
							yOffset < 0 ? obsidian : air
					);
				}
			}
		}

		entity.setLocationAndAngles(x, y, z, entity.rotationYaw, 0.0F);
		entity.motionX = 0.0;
		entity.motionY = 0.0;
		entity.motionZ = 0.0;
	}

	@SuppressWarnings("Duplicates")
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
			NetherPortalType portalType = NetherPortalTeleportHandler.getPortalType(entity);

			if(portalType == null) {
				portalType = NetherPortalTypes.getDefault();
			}

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
							final NetherPortal portal =
									NetherPortalSavedData.get(world).getPortal(pos1);

							if(portal != null && portal.getType() == portalType) {
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

		final Frame frame;
		BlockPos framePos = pos.down();
		final IBlockState frameState = world.getBlockState(framePos);

		if(NetherPortalTypes.getValidBlocks().test(world, framePos, frameState)) {
			final BlockPos pos2 = pos;
			frame = NetherPortalFrames.ACTIVATED_FRAMES.detectWithCondition(
					world, framePos,
					potentialFrame -> potentialFrame.getInnerBlockPositions().contains(pos2)
			);
		} else {
			frame = BlockNetherPortal.findFrame(NetherPortalFrames.ACTIVATED_FRAMES, world, pos).
					getValue().getFrame();
		}

		final double xOffset;
		final double zOffset;
		final EnumFacing forwards;

		if(frame == null) {
			xOffset = 0.0;
			zOffset = 0.0;
			forwards = EnumFacing.NORTH;
		} else if(frame.getType().isVertical()) {
			if(frame.getType() == FrameType.VERTICAL_X) {
				xOffset = frame.getWidth() / 2.0;
				zOffset = 0.0;
				forwards = EnumFacing.NORTH;
			} else {
				xOffset = 0.0;
				zOffset = -frame.getWidth() / 2.0 + 1.0;
				forwards = EnumFacing.EAST;
			}

			pos = frame.getBottomLeft().offset(forwards);
		} else {
			xOffset = frame.getWidth() / 2.0;
			zOffset = -0.5;
			forwards = EnumFacing.SOUTH;
			pos = frame.getBottomLeft().offset(EnumFacing.SOUTH);
		}

		final double x = pos.getX() + xOffset;
		final double y = pos.getY() + 1.0;
		final double z = pos.getZ() + zOffset;

		final EnumFacing originalPortalFacing =
				NetherPortalTeleportHandler.getTeleportData(entity).getPortalFacing();

		final float newYaw = yaw -
				originalPortalFacing.getOpposite().getHorizontalIndex() * 90.0F +
				forwards.getHorizontalIndex() * 90.0F;

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
	@SuppressWarnings({"Duplicates", "deprecation"})
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

		NetherPortalType portalType = NetherPortalTeleportHandler.getPortalType(entity);

		if(portalType == null) {
			portalType = NetherPortalTypes.getDefault();
		}

		if(distance < 0.0) {
			portalY = MathHelper.clamp(portalY, 70, world.getActualHeight() - 10);

			for(int i = -1; i <= 1; i++) {
				for(int j = 0; j < 2; j++) {
					for(int yOffset = -1; yOffset < 3; yOffset++) {
						final IBlockState state;

						if(yOffset == -1) {
							final int index = random.nextInt(portalType.frameBlocks.size());
							final FrameBlock frameBlock =
									portalType.frameBlocks.get(index).getActualBlock();
							state = frameBlock.getBlock().getStateFromMeta(frameBlock.meta);
						} else {
							state = air;
						}

						world.setBlockState(new BlockPos(
								portalX + j * xMultiplier + i * zMultiplier,
								portalY + yOffset,
								portalZ + j * zMultiplier - i * xMultiplier
						), state);
					}
				}
			}
		}

		for(int horzOffset = -1; horzOffset < 3; horzOffset++) {
			for(int yOffset = -1; yOffset < 4; yOffset++) {
				final boolean frame =
						horzOffset == -1 || horzOffset == 2 || yOffset == -1 || yOffset == 3;

				if(frame) {
					final int index = random.nextInt(portalType.frameBlocks.size());
					final FrameBlock frameBlock =
							portalType.frameBlocks.get(index).getActualBlock();

					world.setBlockState(new BlockPos(
							portalX + horzOffset * xMultiplier,
							portalY + yOffset,
							portalZ + horzOffset * zMultiplier
					), frameBlock.getBlock().getStateFromMeta(frameBlock.meta), 2);
				}
			}
		}

		new NetherPortalActivator().forcePortalType(portalType).setUserCreated(false).activate(
				world, new BlockPos(portalX, portalY, portalZ)
		);

		return true;
	}

	@Override
	public void placeEntity(World world, Entity entity, float yaw) {
		super.placeEntity(world, entity, yaw);
		NetherPortalTeleportHandler.clearPortalType(entity);
	}

	//Use vanilla End teleportation behavior
	@Override
	public boolean isVanilla() {
		return true;
	}

	public static void register() {
		RPTeleporter.setTeleporter(VEPTeleporter.class);
	}
}
