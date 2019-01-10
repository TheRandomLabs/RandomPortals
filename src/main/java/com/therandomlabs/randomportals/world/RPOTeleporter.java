package com.therandomlabs.randomportals.world;

import com.therandomlabs.randompatches.integration.RPIStaticConfig;
import com.therandomlabs.randompatches.integration.world.RPITeleporter;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.event.NetherPortalEvent;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.NetherPortalActivator;
import com.therandomlabs.randomportals.api.netherportal.PortalBlockRegistry;
import com.therandomlabs.randomportals.api.netherportal.TeleportData;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import com.therandomlabs.randomportals.frame.NetherPortalFrames;
import com.therandomlabs.randomportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
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
import net.minecraftforge.common.MinecraftForge;

public class RPOTeleporter extends Teleporter {
	public RPOTeleporter(WorldServer world) {
		super(world);
	}

	@Override
	public void placeInPortal(Entity entity, float yaw) {
		final TeleportData data = NetherPortalTeleportHandler.getTeleportData(entity);

		if(data != null) {
			final PortalType type = data.getPortalType();

			if(type.destination.teleportToPortal) {
				if(placeInExistingPortal(entity, yaw)) {
					return;
				}

				//Only players can spawn portals for some reason
				if(entity instanceof EntityPlayerMP && type.destination.generatePortalIfNotFound) {
					makePortal(entity);
					placeInExistingPortal(entity, yaw);
					return;
				}
			}
		}

		if(world.provider.getDimensionType() != DimensionType.THE_END) {
			//Then we have no idea where to put the entity
			entity.moveToBlockPosAndAngles(
					world.getTopSolidOrLiquidBlock(world.getSpawnPoint()),
					yaw, entity.prevRotationPitch
			);
			return;
		}

		final IBlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
		final IBlockState air = Blocks.AIR.getDefaultState();

		final BlockPos spawnPos = world.getSpawnCoordinate();

		final int x = spawnPos.getX();
		final int y = spawnPos.getY() - 1;
		final int z = spawnPos.getZ();

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

	@Override
	public boolean placeInExistingPortal(Entity entity, float yaw) {
		final TeleportData data = NetherPortalTeleportHandler.getTeleportData(entity);
		final NetherPortalEvent.Teleport.SearchingForDestination searching =
				new NetherPortalEvent.Teleport.SearchingForDestination(entity, data);

		if(MinecraftForge.EVENT_BUS.post(searching)) {
			//If false is returned, then placeInPortal will call makePortal and then this method
			//again
			return true;
		}

		final long entityChunkPos = ChunkPos.asLong(
				MathHelper.floor(entity.posX),
				MathHelper.floor(entity.posZ)
		);

		final RPOSavedData savedData = RPOSavedData.get(world);
		final String groupID = data.getPortalType().group.toString();
		final NetherPortal sendingPortal = data.getPortal();
		Frame receivingFrame;

		if(RPOConfig.netherPortals.persistentReceivingPortals) {
			receivingFrame = sendingPortal == null ? null : sendingPortal.getReceivingFrame();
		} else {
			receivingFrame = null;
		}

		NetherPortal receivingPortal = null;
		BlockPos portalPos = null;

		if(receivingFrame != null) {
			//Find a portal block on the bottom row since the following code checks
			//for a frame block below
			for(BlockPos innerPos : receivingFrame.getInnerRowFromBottom(1)) {
				if(PortalBlockRegistry.isPortal(world, innerPos)) {
					portalPos = innerPos;
					break;
				}
			}

			if(portalPos == null) {
				sendingPortal.setReceivingFrame(null);
				receivingFrame = null;
			} else {
				receivingPortal = RPOSavedData.get(world).getNetherPortalByInner(portalPos);

				if(receivingPortal != null &&
						!groupID.equals(receivingPortal.getType().group.toString())) {
					sendingPortal.setReceivingFrame(null);
					//setReceivingFrame does not need to be called on receivingPortal since
					//it would have been created after another was destroyed in the same position,
					//so the receiving portal would have been reset
					receivingFrame = null;
				}
			}
		}

		if(receivingFrame == null) {
			final PortalPosition cachedPos = destinationCoordinateCache.get(entityChunkPos);

			if(cachedPos == null) {
				portalPos = findExistingPortal(savedData, entity, groupID);

				if(portalPos == null) {
					return false;
				}

				destinationCoordinateCache.put(
						entityChunkPos,
						new Teleporter.PortalPosition(portalPos, world.getTotalWorldTime())
				);
			} else {
				portalPos = cachedPos;
				cachedPos.lastUpdateTime = world.getTotalWorldTime();
			}
		}

		final Frame frame;
		BlockPos framePos = portalPos.down();
		final IBlockState frameState = world.getBlockState(framePos);

		if(PortalTypes.getValidBlocks().test(world, framePos, frameState)) {
			final BlockPos required = portalPos;
			frame = NetherPortalFrames.ACTIVATED_FRAMES.detectWithCondition(
					world, framePos,
					potentialFrame -> potentialFrame.isInnerBlock(required)
			);
		} else {
			frame = BlockNetherPortal.findFrame(
					NetherPortalFrames.ACTIVATED_FRAMES, world, portalPos
			).getValue().getFrame();
		}

		final double xOffset;
		final double zOffset;
		final EnumFacing forwards;

		if(frame == null) {
			xOffset = 0.0;
			zOffset = 0.0;
			forwards = EnumFacing.NORTH;
		} else {
			if(sendingPortal != null) {
				sendingPortal.setReceivingFrame(frame);

				if(receivingPortal == null) {
					receivingPortal = savedData.getNetherPortalByInner(portalPos);
				}

				if(receivingPortal != null) {
					final Frame sendingFrame = sendingPortal.getFrame();

					if(sendingFrame != null) {
						receivingPortal.setReceivingFrame(sendingFrame);
					}
				}
			}

			final FrameType type = frame.getType();

			if(type.isVertical()) {
				if(type == FrameType.VERTICAL_X) {
					xOffset = frame.getWidth() / 2.0;
					zOffset = 0.75;
					forwards = EnumFacing.SOUTH;
				} else {
					xOffset = 0.75;
					zOffset = -frame.getWidth() / 2.0 + 1.0;
					forwards = EnumFacing.EAST;
				}

				portalPos = frame.getBottomLeft();
			} else {
				xOffset = frame.getWidth() / 2.0;
				zOffset = -0.5;
				forwards = EnumFacing.SOUTH;
				portalPos = frame.getBottomLeft().offset(EnumFacing.SOUTH);
			}
		}

		final double x = portalPos.getX() + xOffset;
		final double y = portalPos.getY() + 1.0;
		final double z = portalPos.getZ() + zOffset;

		final float newYaw = yaw -
				entity.getHorizontalFacing().getHorizontalIndex() * 90.0F +
				forwards.getHorizontalIndex() * 90.0F;

		if(MinecraftForge.EVENT_BUS.post(new NetherPortalEvent.Teleport.DestinationFound(
				entity, data, frame, x, y, z, newYaw, entity.rotationPitch
		))) {
			return true;
		}

		if(entity instanceof EntityPlayerMP) {
			((EntityPlayerMP) entity).connection.setPlayerLocation(
					x, y, z, newYaw, entity.rotationPitch
			);
		} else {
			entity.setLocationAndAngles(x, y, z, newYaw, entity.rotationPitch);
		}

		MinecraftForge.EVENT_BUS.post(new NetherPortalEvent.Teleport.Post(entity, data, frame));

		return true;
	}

	//My attempt to decipher the black magic that is Teleporter.makePortal
	//TODO rewrite, use information from PortalType.destination
	/*
	Looking for the closest suitable location to place a portal, within 16 blocks horizontally
	(but any distance vertically) of the player's destination coordinates. A valid location is
	3*4 buildable blocks with air 4 high above all 12 blocks. When enough space is available,
	the orientation of the portal is random. The closest valid position in 3D distance is always
	picked.

	A valid location exactly 3 wide in the shorter dimension may sometimes not be found, as the
	check for a point fails if the first tried orientation wants that dimension to be 4 wide.
	This is likely a bug.

	If the first check for valid locations fails entirely, the check is redone looking for a
	1*4 expanse of buildable blocks with air 4 high above each.

	If that fails too, a portal is forced at the target coordinates, but with Y constrained to be
	between 70 and 10 less than the world height (i.e. 118 for the Nether or 246 for the
	Overworld). When a portal is forced in this way, a 2*3 platform of obsidian with air 3 high
	above is created at the target location, overwriting whatever might be there.
	*/
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

		PortalType portalType =
				NetherPortalTeleportHandler.getTeleportData(entity).getPortalType();

		if(portalType.destination.generateUsingReceivingDimensionPortalType) {
			portalType = portalType.group.getType(portalType.destination.dimensionID);
		}

		if(distance < 0.0) {
			portalY = MathHelper.clamp(portalY, 70, world.getActualHeight() - 10);

			for(int i = -1; i <= 1; i++) {
				for(int j = 0; j < 2; j++) {
					for(int yOffset = -1; yOffset < 3; yOffset++) {
						final IBlockState state;

						if(yOffset == -1) {
							final int index = random.nextInt(portalType.frame.blocks.size());
							state = portalType.frame.blocks.get(index).getActualState();
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
					final int index = random.nextInt(portalType.frame.blocks.size());

					world.setBlockState(new BlockPos(
							portalX + horzOffset * xMultiplier,
							portalY + yOffset,
							portalZ + horzOffset * zMultiplier
					), portalType.frame.blocks.get(index).getActualState(), 2);
				}
			}
		}

		new NetherPortalActivator().forcePortalType(portalType).setUserCreated(false).activate(
				world, new BlockPos(portalX, portalY, portalZ), null
		);

		return true;
	}

	@Override
	public void placeEntity(World world, Entity entity, float yaw) {
		placeInPortal(entity, yaw);
		NetherPortalTeleportHandler.clearTeleportData(entity);
	}

	@Override
	public boolean isVanilla() {
		return false;
	}

	private BlockPos findExistingPortal(RPOSavedData savedData, Entity entity, String groupID) {
		final BlockPos entityPos = new BlockPos(entity);
		final int entityY = entityPos.getY();

		BlockPos pos = null;
		BlockPos posWithoutReceivingFrame = null;
		double distanceSq = -1.0;

		for(int xOffset = -128; xOffset <= 128; xOffset++) {
			BlockPos checkPos;

			for(int zOffset = -128; zOffset <= 128; zOffset++) {
				BlockPos portalPos = entityPos.add(
						xOffset,
						world.getActualHeight() - 1 - entityY,
						zOffset
				);

				for(; portalPos.getY() >= 0; portalPos = checkPos) {
					checkPos = portalPos.down();

					if(PortalBlockRegistry.isPortal(world, portalPos)) {
						final NetherPortal portal = savedData.getNetherPortalByInner(portalPos);

						if(portal != null && groupID.equals(portal.getType().group.toString())) {
							for(checkPos = portalPos.down();
								PortalBlockRegistry.isPortal(world, checkPos);
								checkPos = checkPos.down()) {
								portalPos = checkPos;
							}

							final double newDistance = portalPos.distanceSq(entityPos);

							if(distanceSq == -1.0 || newDistance < distanceSq) {
								distanceSq = newDistance;
								pos = portalPos;

								if(portal.getReceivingFrame() == null) {
									posWithoutReceivingFrame = pos;
								}
							}
						}
					}
				}
			}
		}

		//Prefer portals without a saved receiving frame
		return posWithoutReceivingFrame == null ? pos : posWithoutReceivingFrame;
	}

	public static void register() {
		if(RPIStaticConfig.replaceTeleporter) {
			RPITeleporter.setTeleporter(RPOTeleporter.class);
		} else {
			RandomPortals.LOGGER.error(
					"RandomPatches Integration's Teleporter replacement has been disabled. " +
							"This will cause issues with Nether portal teleportation."
			);
		}
	}
}
