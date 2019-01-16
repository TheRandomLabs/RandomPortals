package com.therandomlabs.randomportals.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
			final Map.Entry<Boolean, NetherPortal> entry = BlockNetherPortal.findFrame(
					NetherPortalFrames.ACTIVATED_FRAMES, world, portalPos
			);
			frame = entry == null ? null : entry.getValue().getFrame();
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

	@SuppressWarnings("Duplicates")
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

		final int minY;
		final int maxY;

		if(RandomPortals.CUBIC_CHUNKS_INSTALLED) {
			//https://github.com/OpenCubicChunks/CubicChunks/blob/MC_1.12/src/main/java/io/github/
			//opencubicchunks/cubicchunks/core/asm/mixin/fixes/common/MixinTeleporter.java
			minY = entityY - 128;
			maxY = entityY + 128;
		} else {
			minY = 0;
			maxY = world.getActualHeight();
		}

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		final int worldHeight = type == FrameType.LATERAL ? maxY - 1 : maxY - height;

		for(int checkX = entityX - 16; checkX <= entityX + 16; checkX++) {
			final double xDistance = checkX + 0.5 - entity.posX;

			for(int checkZ = entityZ - 16; checkZ <= entityZ + 16; checkZ++) {
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
						final int offsetX;
						final int offsetZ;

						if(type == FrameType.VERTICAL_Z) {
							offsetX = portalX + lengthOffset;
							offsetZ = portalZ + widthOffset;
						} else {
							offsetX = portalX + widthOffset;
							offsetZ = portalZ + lengthOffset;
						}

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
			topLeft = topLeft.offset(EnumFacing.DOWN);
		} else {
			//portalX, portalY, portalZ point to the bottom left of the frame, so we offset the
			//position in type.getHeightDirection().getOpposite(), then offset it an extra time
			//so that the bottom is in the platform
			//Then we offset it once so the frame is centered in the platform
			topLeft = topLeft.offset(EnumFacing.UP, height - 2).
					offset(type == FrameType.VERTICAL_Z ? EnumFacing.EAST : EnumFacing.SOUTH);
		}

		final int receivingDestination = portalType.getDestinationDimensionID(dimensionID);
		final int sendingDimensionID = data.getSendingPortalWorld().provider.getDimension();
		final Frame receivingFrame = receivingDestination == sendingDimensionID ? frame : null;

		final Frame newFrame = new Frame(world, type, topLeft, width, height);

		if(clone) {
			final List<IBlockState> sendingFrameBlocks = frame.getFrameBlocks();
			final List<BlockPos> framePositions = newFrame.getFrameBlockPositions();
			final int framePositionsSize =
					Math.min(framePositions.size(), sendingFrameBlocks.size());

			for(int i = 0; i < framePositionsSize; i++) {
				world.setBlockState(framePositions.get(i), sendingFrameBlocks.get(i), 2);
			}

			final List<IBlockState> sendingInnerBlocks = frame.getInnerBlocks();
			final List<BlockPos> innerPositions = newFrame.getInnerBlockPositions();
			final int innerPositionsSize =
					Math.min(innerPositions.size(), sendingInnerBlocks.size());

			for(int i = 0; i < innerPositionsSize; i++) {
				world.setBlockState(innerPositions.get(i), sendingInnerBlocks.get(i), 2);
			}

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

	@SuppressWarnings("Duplicates")
	public boolean isValidPortalPosition(BlockPos.MutableBlockPos pos, int x, int y, int z,
			int platformWidth, int platformLength, int spaceHeight, FrameType type) {
		//y - 1 should be the platform
		//Ensure the platform (size platformWidth * platformHeight) is solid
		for(int widthOffset = 0; widthOffset < platformWidth; widthOffset++) {
			for(int lengthOffset = 0; lengthOffset < platformLength; lengthOffset++) {
				final int offsetX;
				final int offsetZ;

				if(type == FrameType.VERTICAL_Z) {
					offsetX = x + lengthOffset;
					offsetZ = z + widthOffset;
				} else {
					offsetX = x + widthOffset;
					offsetZ = z + lengthOffset;
				}

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

	@SuppressWarnings("Duplicates")
	public BlockPos findExistingPortal(RPOSavedData savedData, Entity entity, String groupID,
			boolean oneWay) {
		final String defaultGroupID = PortalTypes.getDefaultGroup().toString();

		final BlockPos entityPos = new BlockPos(entity);
		final int entityY = entityPos.getY();

		final int minY;
		final int maxY;

		if(RandomPortals.CUBIC_CHUNKS_INSTALLED) {
			//https://github.com/OpenCubicChunks/CubicChunks/blob/MC_1.12/src/main/java/io/github/
			//opencubicchunks/cubicchunks/core/asm/mixin/fixes/common/MixinTeleporter.java
			minY = entityY - 128;
			maxY = entityY + 128;
		} else {
			minY = 0;
			maxY = world.getActualHeight();
		}

		BlockPos pos = null;
		double distanceSq = -1.0;

		BlockPos preferedPos = null;
		double preferedDistanceSq = -1.0;

		for(int xOffset = -128; xOffset <= 128; xOffset++) {
			BlockPos checkPos;

			for(int zOffset = -128; zOffset <= 128; zOffset++) {
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
}
