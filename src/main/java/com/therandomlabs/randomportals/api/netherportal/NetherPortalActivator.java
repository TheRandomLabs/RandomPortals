package com.therandomlabs.randomportals.api.netherportal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.advancements.RPOCriteriaTriggers;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypeGroup;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.event.NetherPortalEvent;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import com.therandomlabs.randomportals.block.RPOBlocks;
import com.therandomlabs.randomportals.frame.NetherPortalFrames;
import com.therandomlabs.randomportals.handler.NetherPortalActivationHandler;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class NetherPortalActivator {
	private static final class PortalContainer {
		NetherPortal portal;
	}

	private static final Random random = new Random();

	private PortalType forcePortalType;
	private PortalType[] portalTypes;
	private boolean userCreated = true;
	private boolean activatedByFire;
	private boolean activationDelayed;
	private FunctionType functionType;
	private EntityPlayer player;

	public PortalType getForcedPortalType() {
		return forcePortalType;
	}

	public NetherPortalActivator forcePortalType(PortalType type) {
		forcePortalType = type;
		return this;
	}

	public PortalType[] getPortalTypes() {
		return portalTypes == null ? null : portalTypes.clone();
	}

	public NetherPortalActivator setPortalTypes(PortalType... portalTypes) {
		if(portalTypes.length == 0) {
			portalTypes = null;
		} else {
			for(PortalType type : portalTypes) {
				if(type == null) {
					throw new NullPointerException("portalTypes");
				}
			}
		}

		if(portalTypes != null && forcePortalType != null) {
			throw new IllegalStateException(
					"setPortalTypes cannot be called when forcePortalType is not null"
			);
		}

		this.portalTypes = portalTypes;
		return this;
	}

	public boolean isUserCreated() {
		return userCreated;
	}

	public NetherPortalActivator setUserCreated(boolean flag) {
		userCreated = flag;
		return this;
	}

	public boolean isActivatedByFire() {
		return activatedByFire;
	}

	public NetherPortalActivator setActivatedByFire(boolean flag) {
		activatedByFire = flag;
		return this;
	}

	public boolean isActivationDelayed() {
		return activationDelayed;
	}

	public NetherPortalActivator setActivationDelayed(boolean flag) {
		activationDelayed = flag;
		return this;
	}

	public FunctionType getFunctionType() {
		return functionType;
	}

	public NetherPortalActivator setFunctionType(FunctionType type) {
		functionType = type;
		return this;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

	public NetherPortalActivator setPlayer(EntityPlayer player) {
		this.player = player;
		return this;
	}

	public NetherPortal activate(World world, BlockPos pos, ItemStack activator) {
		return activate(world, pos, activator, (axis, color) -> {
			final IBlockState state;

			switch(axis) {
			case X:
				state = ((BlockNetherPortal) Blocks.PORTAL).getByColor(color).getDefaultState();
				break;
			case Y:
				state = RPOBlocks.purple_lateral_nether_portal.getByColor(color).getDefaultState();
				break;
			default:
				state = ((BlockNetherPortal) Blocks.PORTAL).getByColor(color).getDefaultState().
						withProperty(BlockPortal.AXIS, EnumFacing.Axis.Z);
			}

			return state.withProperty(BlockNetherPortal.USER_PLACED, false);
		});
	}

	public NetherPortal activate(World world, BlockPos pos, ItemStack activator,
			IBlockState lateralPortal, IBlockState verticalXPortal, IBlockState verticalZPortal) {
		return activate(world, pos, activator, (axis, color) ->
				FrameType.get(axis, lateralPortal, verticalXPortal, verticalZPortal));
	}

	public NetherPortal activate(World world, BlockPos pos, ItemStack activator,
			BiFunction<EnumFacing.Axis, EnumDyeColor, IBlockState> portalBlocks) {
		if(NetherPortalActivationHandler.isDelayedActivationQueued(world, pos)) {
			return null;
		}

		final FrameStatePredicate validBlocks;

		if(forcePortalType == null) {
			if(portalTypes == null) {
				validBlocks = PortalTypes.getValidBlocks();
			} else {
				validBlocks = PortalTypes.getValidBlocks(portalTypes);
			}
		} else {
			validBlocks = PortalTypes.getValidBlocks(forcePortalType);
		}

		final PortalContainer portal = new PortalContainer();
		BlockPos framePos = null;
		FrameType type = FrameType.LATERAL_OR_VERTICAL;

		for(EnumFacing facing : EnumFacing.values()) {
			final BlockPos offset = pos.offset(facing);
			final IBlockState state = world.getBlockState(offset);

			if(!validBlocks.test(world, pos, state)) {
				continue;
			}

			NetherPortalFrames.EMPTY_FRAMES.detectWithCondition(world, offset, type, frame -> {
				final NetherPortal result =
						testFrame(frame, offset, facing.getOpposite(), activator);

				if(result == null) {
					return false;
				}

				portal.portal = result;
				return true;
			});

			if(portal.portal != null) {
				framePos = offset;
				break;
			}

			//Optimization black magic
			if(facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
				type = FrameType.LATERAL;
			} else {
				break;
			}
		}

		if(portal.portal == null) {
			return null;
		}

		final NetherPortalEvent.Activate event = new NetherPortalEvent.Activate(
				world, portal.portal, framePos, userCreated, activatedByFire
		);

		if(MinecraftForge.EVENT_BUS.post(event)) {
			return null;
		}

		final NetherPortal result = event.getPortal();
		onActivate(world, result, pos, portalBlocks);
		return result;
	}

	protected void onActivate(World world, NetherPortal portal, BlockPos pos,
			BiFunction<EnumFacing.Axis, EnumDyeColor, IBlockState> portalBlocks) {
		RPOSavedData.get(world).addNetherPortal(portal, userCreated);

		final Frame frame = portal.getFrame();
		final EnumFacing.Axis axis = frame.getType().getAxis();
		final EnumDyeColor[] colors = portal.getType().color.colors;
		final IBlockState state = portalBlocks.apply(axis, colors[random.nextInt(colors.length)]);
		final Block block = state.getBlock();
		final Class<?> blockClass = block.getClass();
		final BlockNetherPortal portalBlock =
				block instanceof BlockNetherPortal ? (BlockNetherPortal) block : null;

		final List<BlockPos> portalPositions = new ArrayList<>();

		for(BlockPos innerPos : frame.getInnerBlockPositions()) {
			//Allow players to create colorful patterns
			if(portalBlock != null &&
					!RPOConfig.netherPortals.replaceUserPlacedPortalsOnActivation) {
				final IBlockState innerState = world.getBlockState(innerPos);

				if(innerState.getBlock().getClass() == blockClass &&
						innerState.getValue(BlockNetherPortal.USER_PLACED) &&
						portalBlock.getEffectiveAxis(innerState) == axis) {
					continue;
				}
			}

			portalPositions.add(innerPos);
		}

		if(activationDelayed) {
			NetherPortalActivationHandler.queueDelayedActivation(world, portalPositions, state);
		} else {
			for(BlockPos portalPos : portalPositions) {
				world.setBlockState(portalPos, state, 2);
			}
		}

		final PortalType portalType = portal.getType();
		final SoundEvent[] sounds = portalType.activation.getActivationSoundEvents();

		if(sounds.length != 0) {
			world.playSound(
					null,
					pos,
					sounds[world.rand.nextInt(sounds.length)],
					SoundCategory.BLOCKS,
					1.0F,
					world.rand.nextFloat() * 0.4F + 0.8F
			);
		}

		if(player != null && RPOConfig.misc.advancements &&
				portalType.group.toString().equals(PortalTypes.VANILLA_NETHER_PORTAL_ID)) {
			final EntityPlayerMP playerMP = (EntityPlayerMP) player;

			RPOCriteriaTriggers.PORTALS.trigger(playerMP);
			RPOCriteriaTriggers.ACTIVATED_NETHER_PORTAL.trigger(
					playerMP, frame.getType(), frame.getSize()
			);
		}
	}

	protected NetherPortal testFrame(Frame frame, BlockPos framePos, EnumFacing inwards,
			ItemStack activator) {
		if(!frame.isFacingInwards(framePos, inwards)) {
			return null;
		}

		if(forcePortalType != null) {
			return new NetherPortal(frame, null, forcePortalType, functionType);
		}

		final List<PortalType> types;

		if(portalTypes == null) {
			final int dimensionID = frame.getWorld().provider.getDimension();
			types = new ArrayList<>();

			for(PortalTypeGroup group : PortalTypes.getGroups().values()) {
				final PortalType type = group.types.get(dimensionID);

				if(type == null) {
					if(group.testActivationDimensionID(dimensionID)) {
						types.add(group.getDefaultType());
					}
				} else {
					types.add(type);
				}
			}
		} else {
			types = Arrays.asList(portalTypes);
		}

		for(PortalType type : types) {
			if((!activatedByFire || type.activation.canBeActivatedByFire) && type.test(frame) &&
					(forcePortalType != null || activator == null ||
							type.testActivator(activator))) {
				return new NetherPortal(frame, null, type, functionType);
			}
		}

		return null;
	}
}
