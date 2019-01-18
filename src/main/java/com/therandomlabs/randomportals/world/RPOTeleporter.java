package com.therandomlabs.randomportals.world;

import java.util.Collections;
import java.util.List;
import com.therandomlabs.randompatches.common.RPTeleporter;
import com.therandomlabs.randompatches.config.RPStaticConfig;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.api.config.DestinationData;
import com.therandomlabs.randomportals.api.config.FrameSize;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.event.NetherPortalEvent;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.netherportal.FunctionType;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.NetherPortalActivator;
import com.therandomlabs.randomportals.api.netherportal.PortalBlockRegistry;
import com.therandomlabs.randomportals.api.netherportal.TeleportData;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import com.therandomlabs.randomportals.frame.NetherPortalFrames;
import com.therandomlabs.randomportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

public class RPOTeleporter extends Teleporter {
	private final int dimensionID;

	public RPOTeleporter(WorldServer world) {
		super(world);
		dimensionID = world.provider.getDimension();
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
		final PortalType portalType = data.getPortalType();
		final String groupID = portalType.group.toString();
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
				portalPos = findExistingPortal(
						savedData, entity, groupID, portalType.destination.oneWay
				);

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
			final Tuple<Boolean, NetherPortal> entry = BlockNetherPortal.findFrame(
					NetherPortalFrames.ACTIVATED_FRAMES, world, portalPos
			);
			frame = entry == null ? null : entry.getSecond().getFrame();
		}

		double xOffset;
		double zOffset;
		final EnumFacing forwards;
		BlockPos teleportPos = portalPos;

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
					final int receivingDestination =
							receivingPortal.getType().getDestinationDimensionID(dimensionID);
					final int sendingDimensionID =
							data.getSendingPortalWorld().provider.getDimension();

					if(receivingDestination == sendingDimensionID) {
						receivingPortal.setReceivingFrame(sendingPortal.getFrame());
					}
				}
			}

			final FrameType type = frame.getType();
			final int width = frame.getWidth();
			final BlockPos bottomLeft = frame.getBottomLeft();

			if(type.isVertical()) {
				if(type == FrameType.VERTICAL_X) {
					xOffset = width / 2.0;
					zOffset = 0.75;
					forwards = EnumFacing.SOUTH;
				} else {
					xOffset = 0.75;
					zOffset = -width / 2.0 + 1.0;
					forwards = EnumFacing.EAST;
				}

				teleportPos = bottomLeft.up();
			} else {
				xOffset = width / 2.0;
				zOffset = 0.5;
				forwards = EnumFacing.SOUTH;
				teleportPos = bottomLeft.up();

				final int xOffsetInt = (int) xOffset;
				final BlockPos actualPos = teleportPos.east(xOffsetInt);
				boolean invalid = isSolidOrLiquid(world, actualPos) ||
						isSolidOrLiquid(world, actualPos.up());

				//Then the player is meant to be teleported between two blocks, so we check
				//the east block as well
				if(!invalid && xOffset != xOffsetInt) {
					final BlockPos east = actualPos.east();
					invalid = isSolidOrLiquid(world, east) || isSolidOrLiquid(world, east.up());
				}

				if(invalid) {
					xOffset = 0.5;

					boolean found = false;

					for(BlockPos pos : frame.getFrameBlockPositions()) {
						final BlockPos up = pos.up();

						if(!isSolidOrLiquid(world, up) && !isSolidOrLiquid(world, up.up())) {
							teleportPos = up;
							found = true;
							break;
						}
					}

					if(!found) {
						final int x = actualPos.getX();
						int y = actualPos.getY();
						final int z = actualPos.getZ();

						final int maxY;

						if(RandomPortals.CUBIC_CHUNKS_INSTALLED) {
							maxY = y + RPOConfig.netherPortals.portalSearchRadius;
						} else {
							maxY = world.getActualHeight();
						}

						y++;

						teleportPos = actualPos;

						for(; y <= maxY; y++) {
							teleportPos = new BlockPos(x, y, z);

							if(!isSolidOrLiquid(world, teleportPos) &&
									!isSolidOrLiquid(world, teleportPos.up())) {
								break;
							}
						}
					}
				}
			}
		}

		final double x = teleportPos.getX() + xOffset;
		final double y = teleportPos.getY();
		final double z = teleportPos.getZ() + zOffset;

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

	@Override
	public boolean makePortal(Entity entity) {
		final TeleportData data = NetherPortalTeleportHandler.getTeleportData(entity);
		PortalType portalType = data.getPortalType();

		final Frame frame = data.getFrame();

		final boolean clone = frame != null && portalType.destination.portalGenerationBehavior ==
				DestinationData.PortalGenerationBehavior.CLONE;

		final FrameType type;

		int width;
		int height;

		if(clone) {
			type = frame.getType();
			width = frame.getWidth();
			height = frame.getHeight();
		} else {
			final List<FrameType> types;

			if(portalType.destination.generatedFrameType == FrameType.SAME) {
				if(frame == null) {
					types = FrameType.LATERAL_OR_VERTICAL.getTypes();
				} else if(frame.getType() == FrameType.LATERAL) {
					types = Collections.singletonList(FrameType.LATERAL);
				} else {
					types = FrameType.VERTICAL.getTypes();
				}
			} else {
				types = portalType.destination.generatedFrameType.getTypes();
			}

			type = types.get(random.nextInt(types.size()));

			final FrameSize size = portalType.destination.getGeneratedFrameSize(frame).get(type);

			if(size.minWidth == size.maxWidth) {
				width = size.maxWidth;
			} else {
				width = random.nextInt(size.maxWidth + 1 - size.minWidth) + size.minWidth;
			}

			if(size.minHeight == size.maxHeight) {
				height = size.maxHeight;
			} else {
				height = random.nextInt(size.maxHeight + 1 - size.minHeight) + size.minHeight;
			}
		}

		final boolean oneWay = portalType.destination.oneWay;

		if(portalType.destination.portalGenerationBehavior ==
				DestinationData.PortalGenerationBehavior.USE_RECEIVING_DIMENSION_PORTAL_TYPE) {
			portalType = portalType.group.getType(portalType.destination.dimensionID);
		}

		final int platformWidth = width;
		final int platformLength = type == FrameType.LATERAL ? height : 3;
		final int spaceHeight = type == FrameType.LATERAL ? 2 : height;

		final int fallbackWidth = width == 3 ? 3 : 4;
		final int fallbackLength = 1;
		final int fallbackSpaceHeight = type == FrameType.LATERAL ? 2 : 4;

		final int entityX = (int) entity.posX;
		final int entityY = (int) entity.posY;
		final int entityZ = (int) entity.posZ;

		int portalX = 0;
		int portalY = 0;
		int portalZ = 0;

		double distanceSq = -1.0;

		int fallbackX = 0;
		int fallbackY = 0;
		int fallbackZ = 0;

		double fallbackDistanceSq = -1.0;

		final Tuple<Integer, Integer> yBounds = getYBounds(entityY);
		final int minY = yBounds.getFirst();
		final int maxY = yBounds.getSecond();

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		final int worldHeight = type == FrameType.LATERAL ? maxY - 1 : maxY - height;

		final int radius = RPOConfig.netherPortals.portalGenerationLocationSearchRadius;

		for(int checkX = entityX - radius; checkX <= entityX + radius; checkX++) {
			final double xDistance = checkX + 0.5 - entity.posX;

			for(int checkZ = entityZ - radius; checkZ <= entityZ + radius; checkZ++) {
				final double zDistance = checkZ + 0.5 - entity.posZ;

				for(int checkY = worldHeight; checkY >= minY; checkY--) {
					//Check for space above the platform first
					if(!world.isAirBlock(pos.setPos(checkX, checkY, checkZ))) {
						continue;
					}

					//Find the highest air block with a non-air block below it
					while(checkY > minY &&
							world.isAirBlock(pos.setPos(checkX, checkY - 1, checkZ))) {
						checkY--;
					}

					final double yDistance = checkY + 0.5 - entity.posY;
					final double newDistance = xDistance * xDistance +
							yDistance * yDistance +
							zDistance * zDistance;

					if(distanceSq != -1.0 && newDistance > distanceSq) {
						continue;
					}

					boolean valid = isValidPortalPosition(
							pos, checkX, checkY, checkZ, platformWidth, platformLength,
							spaceHeight, type
					);

					if(valid) {
						distanceSq = newDistance;
						portalX = checkX;
						portalY = checkY;
						portalZ = checkZ;

						continue;
					}

					if(distanceSq != -1.0 ||
							(fallbackDistanceSq != -1.0 && newDistance >= fallbackDistanceSq)) {
						continue;
					}

					valid = isValidPortalPosition(
							pos, checkX, checkY, checkZ, fallbackWidth, fallbackLength,
							fallbackSpaceHeight, type
					);

					if(valid) {
						fallbackDistanceSq = newDistance;
						fallbackX = checkX;
						fallbackY = checkY;
						fallbackZ = checkZ;
					}
				}
			}
		}

		final RPOSavedData savedData = RPOSavedData.get(world);
		final IBlockState air = Blocks.AIR.getDefaultState();

		if(distanceSq == -1.0) {
			if(fallbackDistanceSq == -1.0) {
				portalX = entityX;
				portalY = MathHelper.clamp((int) entity.posY, 70, maxY - 10);
				portalZ = entityZ;

				for(int widthOffset = 0; widthOffset < platformWidth; widthOffset++) {
					for(int lengthOffset = 0; lengthOffset < platformLength; lengthOffset++) {
						final Tuple<Integer, Integer> offsets =
								getOffsets(type, widthOffset, lengthOffset);
						final int offsetX = portalX + offsets.getFirst();
						final int offsetZ = portalZ + offsets.getSecond();

						//Don't use a MutableBlockPos because we're adding this to the saved data
						final BlockPos platformPos = new BlockPos(offsetX, portalY - 1, offsetZ);
						savedData.addGeneratedNetherPortalFrame(platformPos, portalType);

						final int index = random.nextInt(portalType.frame.blocks.size());
						world.setBlockState(
								platformPos, portalType.frame.blocks.get(index).getActualState(), 2
						);

						for(int yOffset = 0; yOffset < spaceHeight; yOffset++) {
							world.setBlockState(
									pos.setPos(offsetX, portalY + yOffset, offsetZ), air, 2
							);
						}
					}
				}
			} else {
				portalX = fallbackX;
				portalY = fallbackY;
				portalZ = fallbackZ;
				width = 4;
				height = 5;
			}
		}

		BlockPos topLeft = new BlockPos(portalX, portalY, portalZ);

		if(type == FrameType.LATERAL) {
			topLeft = topLeft.down();
		} else {
			//portalX, portalY, portalZ point to the bottom left of the frame, so we offset the
			//position in type.getHeightDirection().getOpposite(), then offset it an extra time
			//so that the bottom is in the platform
			//Then we offset it once so the frame is centered in the platform
			topLeft = topLeft.up(height - 2).
					offset(type == FrameType.VERTICAL_Z ? EnumFacing.EAST : EnumFacing.SOUTH);
		}

		final int receivingDestination = portalType.getDestinationDimensionID(dimensionID);
		final int sendingDimensionID = data.getSendingPortalWorld().provider.getDimension();
		final Frame receivingFrame = receivingDestination == sendingDimensionID ? frame : null;

		final Frame newFrame = new Frame(world, type, topLeft, width, height);

		if(clone) {
			clone(newFrame.getFrameBlockPositions(), frame.getFrameBlocks());
			clone(newFrame.getInnerBlockPositions(), frame.getInnerBlocks());

			final NetherPortal portal = new NetherPortal(
					newFrame, receivingFrame, portalType, oneWay ? FunctionType.ONE_WAY : null
			);

			RPOSavedData.get(world).addNetherPortal(portal, false);

			return true;
		}

		for(BlockPos framePos : newFrame.getFrameBlockPositions()) {
			final int index = random.nextInt(portalType.frame.blocks.size());

			world.setBlockState(
					framePos, portalType.frame.blocks.get(index).getActualState(), 2
			);
		}

		for(BlockPos innerPos : newFrame.getInnerBlockPositions()) {
			world.setBlockState(innerPos, air, 2);
		}

		//We offset the position once in type.getWidthDirection() and type.getHeightDirection()
		//because NetherPortalActivator only works with inner blocks
		final BlockPos activationPos =
				topLeft.offset(type.getWidthDirection()).offset(type.getHeightDirection());

		new NetherPortalActivator().
				forcePortalType(portalType).
				setUserCreated(false).
				setFunctionType(oneWay ? FunctionType.ONE_WAY : null).
				activate(world, activationPos, null).
				setReceivingFrame(receivingFrame);

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

	public boolean isValidPortalPosition(BlockPos.MutableBlockPos pos, int x, int y, int z,
			int platformWidth, int platformLength, int spaceHeight, FrameType type) {
		//y - 1 should be the platform
		//Ensure the platform (size platformWidth * platformHeight) is solid
		for(int widthOffset = 0; widthOffset < platformWidth; widthOffset++) {
			for(int lengthOffset = 0; lengthOffset < platformLength; lengthOffset++) {
				final Tuple<Integer, Integer> offsets =
						getOffsets(type, widthOffset, lengthOffset);
				final int offsetX = x + offsets.getFirst();
				final int offsetZ = z + offsets.getSecond();

				if(world.isAirBlock(pos.setPos(offsetX, y - 1, offsetZ)) ||
						!world.getBlockState(pos).getMaterial().isSolid()) {
					return false;
				}

				for(int yOffset = 0; yOffset < spaceHeight; yOffset++) {
					if(!world.isAirBlock(pos.setPos(offsetX, y + yOffset, offsetZ))) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public BlockPos findExistingPortal(RPOSavedData savedData, Entity entity, String groupID,
			boolean oneWay) {
		final String defaultGroupID = PortalTypes.getDefaultGroup().toString();

		final BlockPos entityPos = new BlockPos(entity);
		final int entityY = entityPos.getY();

		final Tuple<Integer, Integer> yBounds = getYBounds(entityY);
		final int minY = yBounds.getFirst();
		final int maxY = yBounds.getSecond();

		BlockPos pos = null;
		double distanceSq = -1.0;

		BlockPos preferedPos = null;
		double preferedDistanceSq = -1.0;

		final int radius = RPOConfig.netherPortals.portalSearchRadius;

		for(int xOffset = -radius; xOffset <= radius; xOffset++) {
			BlockPos checkPos;

			for(int zOffset = -radius; zOffset <= radius; zOffset++) {
				BlockPos portalPos = entityPos.add(
						xOffset,
						maxY - 1 - entityY,
						zOffset
				);

				for(; portalPos.getY() >= minY; portalPos = checkPos) {
					checkPos = portalPos.down();

					if(PortalBlockRegistry.isPortal(world, portalPos)) {
						final NetherPortal portal = savedData.getNetherPortalByInner(portalPos);
						final FunctionType functionType =
								portal == null ? null : portal.getFunctionType();

						if(functionType == FunctionType.DECORATIVE ||
								(oneWay && functionType != FunctionType.ONE_WAY)) {
							continue;
						}

						if((portal != null && !groupID.equals(portal.getType().group.toString())) ||
								(portal == null && !groupID.equals(defaultGroupID))) {
							continue;
						}

						for(checkPos = portalPos.down();
							PortalBlockRegistry.isPortal(world, checkPos);
							checkPos = checkPos.down()) {
							portalPos = checkPos;
						}

						final double newDistance = portalPos.distanceSq(entityPos);

						if(distanceSq == -1.0 || newDistance < distanceSq) {
							distanceSq = newDistance;
							pos = portalPos;
						}

						if((portal == null || portal.getReceivingFrame() == null) &&
								(preferedDistanceSq == -1.0 || newDistance < preferedDistanceSq)) {
							preferedPos = pos;
						}
					}
				}
			}
		}

		//Prefer portals without a saved receiving frame
		return preferedPos == null ? pos : preferedPos;
	}

	public static boolean isSolidOrLiquid(World world, BlockPos pos) {
		final IBlockState state = world.getBlockState(pos);
		final Material material = state.getMaterial();

		return (material.blocksMovement() || material.isLiquid()) &&
				!state.getBlock().isLeaves(state, world, pos) &&
				!state.getBlock().isFoliage(world, pos);
	}

	public static void register() {
		if(RPStaticConfig.replaceTeleporter) {
			RPTeleporter.setTeleporter(RPOTeleporter.class);
		} else {
			RandomPortals.LOGGER.error(
					"RandomPatches' Teleporter replacement has been disabled. " +
							"This will cause issues with Nether portal teleportation."
			);
		}
	}

	private Tuple<Integer, Integer> getYBounds(int referenceY) {
		if(RandomPortals.CUBIC_CHUNKS_INSTALLED) {
			return new Tuple<>(
					referenceY - RPOConfig.netherPortals.portalSearchRadius,
					referenceY + RPOConfig.netherPortals.portalSearchRadius
			);
		}

		return new Tuple<>(0, world.getActualHeight());
	}

	private void clone(List<BlockPos> newPositions, List<IBlockState> oldStates) {
		final int size = Math.min(newPositions.size(), oldStates.size());

		for(int i = 0; i < size; i++) {
			world.setBlockState(newPositions.get(i), oldStates.get(i), 2);
		}
	}

	private static Tuple<Integer, Integer> getOffsets(FrameType type, int widthOffset,
			int lengthOffset) {
		return type == FrameType.VERTICAL_Z ?
				new Tuple<>(lengthOffset, widthOffset) : new Tuple<>(widthOffset, lengthOffset);
	}
}
