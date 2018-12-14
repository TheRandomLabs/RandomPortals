package com.therandomlabs.randomportals.world.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class RPOSavedData extends WorldSavedData {
	public static final String ID = "randomportals";

	public static final String NETHER_PORTALS_KEY = "NetherPortals";

	public static final String FRAME_KEY = "Frame";
	public static final String RECEIVING_FRAME_KEY = "ReceivingFrame";
	public static final String PORTAL_TYPE_KEY = "PortalType";

	public static final String FRAME_TYPE_KEY = "FrameType";
	public static final String TOP_LEFT_KEY = "TopLeft";
	public static final String WIDTH_KEY = "Width";
	public static final String HEIGHT_KEY = "Height";

	public static final String GENERATED_NETHER_PORTAL_FRAMES_KEY = "GeneratedNetherPortalFrames";

	public static final String END_PORTALS_KEY = "EndPortals";

	private static final FrameType[] TYPES = FrameType.values();

	private final Map<BlockPos, NetherPortal> netherPortals = new HashMap<>();
	private final Map<String, Set<BlockPos>> generatedNetherPortalFrames = new HashMap<>();
	private final Map<BlockPos, Frame> endPortals = new HashMap<>();

	public RPOSavedData() {
		super(ID);
	}

	public RPOSavedData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		netherPortals.clear();
		generatedNetherPortalFrames.clear();
		endPortals.clear();

		for(NBTBase tag : nbt.getTagList(NETHER_PORTALS_KEY, Constants.NBT.TAG_COMPOUND)) {
			final NBTTagCompound compound = (NBTTagCompound) tag;

			final Frame frame = readFrame(compound.getCompoundTag(FRAME_KEY));

			//Saved with old version - remove when 1.12.2-1.0.0.0 is released
			if(frame == null) {
				continue;
			}

			final Frame receivingFrame = readFrame(compound.getCompoundTag(RECEIVING_FRAME_KEY));
			final NetherPortalType type =
					NetherPortalTypes.get(compound.getString(PORTAL_TYPE_KEY));

			netherPortals.put(frame.getTopLeft(), new NetherPortal(frame, receivingFrame, type));
		}

		final NBTTagCompound compound = nbt.getCompoundTag(GENERATED_NETHER_PORTAL_FRAMES_KEY);

		for(String typeName : compound.getKeySet()) {
			final NBTTagList list = compound.getTagList(typeName, Constants.NBT.TAG_COMPOUND);
			final Set<BlockPos> positions = new HashSet<>(list.tagCount());

			for(NBTBase tag : list) {
				positions.add(NBTUtil.getPosFromTag((NBTTagCompound) tag));
			}

			generatedNetherPortalFrames.merge(
					//If the type name is invalid, the default type's name is returned
					NetherPortalTypes.get(typeName).getName(),
					positions,
					(a, b) -> {
						a.addAll(b);
						return a;
					}
			);
		}

		for(NBTBase tag : nbt.getTagList(END_PORTALS_KEY, Constants.NBT.TAG_COMPOUND)) {
			final Frame frame = readFrame((NBTTagCompound) tag);
			endPortals.put(frame.getTopLeft(), frame);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		final NBTTagList netherPortalList = new NBTTagList();

		for(NetherPortal portal : netherPortals.values()) {
			final NBTTagCompound compound = new NBTTagCompound();

			final NBTTagCompound frame = writeFrame(new NBTTagCompound(), portal.getFrame());
			final NBTTagCompound receivingFrame =
					writeFrame(new NBTTagCompound(), portal.getReceivingFrame());

			compound.setTag(FRAME_KEY, frame);
			compound.setTag(RECEIVING_FRAME_KEY, receivingFrame);
			compound.setString(PORTAL_TYPE_KEY, portal.getType().getName());

			netherPortalList.appendTag(compound);
		}

		nbt.setTag(NETHER_PORTALS_KEY, netherPortalList);

		final NBTTagCompound generatedPortalFramesTag = new NBTTagCompound();

		for(Map.Entry<String, Set<BlockPos>> entry : generatedNetherPortalFrames.entrySet()) {
			final NBTTagList positionList = new NBTTagList();

			for(BlockPos pos : entry.getValue()) {
				positionList.appendTag(NBTUtil.createPosTag(pos));
			}

			generatedPortalFramesTag.setTag(
					//If the type name is invalid, the default type's name is returned
					NetherPortalTypes.get(entry.getKey()).getName(),
					positionList
			);
		}

		nbt.setTag(GENERATED_NETHER_PORTAL_FRAMES_KEY, generatedPortalFramesTag);

		final NBTTagList endPortalList = new NBTTagList();

		for(Frame frame : endPortals.values()) {
			endPortalList.appendTag(writeFrame(new NBTTagCompound(), frame));
		}

		nbt.setTag(END_PORTALS_KEY, endPortalList);

		return nbt;
	}

	public Map<BlockPos, NetherPortal> getNetherPortals() {
		return netherPortals;
	}

	public Map<BlockPos, NetherPortal> getUserCreatedNetherPortals(World world) {
		if(world != null) {
			for(NetherPortal portal : netherPortals.values()) {
				portal.getFrame().setWorld(world);
			}
		}

		return netherPortals;
	}

	public NetherPortal getNetherPortal(BlockPos portalPos) {
		return getNetherPortal(null, portalPos);
	}

	public NetherPortal getNetherPortal(World world, BlockPos portalPos) {
		return getNetherPortal(frame -> frame.isInnerBlock(portalPos), world, portalPos);
	}

	public NetherPortal getNetherPortalByFrame(BlockPos portalPos) {
		return getNetherPortalByFrame(null, portalPos);
	}

	public NetherPortal getNetherPortalByFrame(World world, BlockPos framePos) {
		return getNetherPortal(frame -> frame.isFrameBlock(framePos), world, framePos);
	}

	public NetherPortal getNetherPortalByTopLeft(BlockPos topLeft) {
		return netherPortals.get(topLeft);
	}

	public NetherPortal addNetherPortal(Frame frame, NetherPortalType type, boolean userCreated) {
		final NetherPortal portal = new NetherPortal(frame, null, type);
		addNetherPortal(portal, userCreated);
		return portal;
	}

	public void addNetherPortal(NetherPortal portal, boolean userCreated) {
		final Frame frame = portal.getFrame();

		netherPortals.put(frame.getTopLeft(), portal);

		if(!userCreated) {
			generatedNetherPortalFrames.merge(
					portal.getType().getName(),
					new HashSet<>(frame.getFrameBlockPositions()),
					(a, b) -> {
						a.addAll(b);
						return a;
					}
			);
		}

		markDirty();
	}

	public NetherPortal removeNetherPortal(BlockPos portalPos) {
		return removeNetherPortal(frame -> frame.isInnerBlock(portalPos), portalPos);
	}

	public NetherPortal removeNetherPortalByFrame(BlockPos portalPos) {
		return removeNetherPortal(frame -> frame.isFrameBlock(portalPos), portalPos);
	}

	public NetherPortalType getGeneratedNetherPortalType(BlockPos framePos) {
		for(Map.Entry<String, Set<BlockPos>> entry : generatedNetherPortalFrames.entrySet()) {
			if(entry.getValue().contains(framePos)) {
				return NetherPortalTypes.get(entry.getKey());
			}
		}

		return null;
	}

	public void removeGeneratedNetherPortalFramePos(String typeName, BlockPos framePos) {
		final Set<BlockPos> positions = generatedNetherPortalFrames.get(typeName);

		if(positions != null) {
			markDirty();
		}
	}

	public Map<BlockPos, Frame> getEndPortals() {
		return endPortals;
	}

	public Frame getEndPortal(BlockPos portalPos) {
		return getEndPortal(null, portalPos);
	}

	public Frame getEndPortal(World world, BlockPos portalPos) {
		return getEndPortal(frame -> frame.isInnerBlock(portalPos), world, portalPos);
	}

	public Frame getEndPortalByFrame(BlockPos portalPos) {
		return getEndPortalByFrame(null, portalPos);
	}

	public Frame getEndPortalByFrame(World world, BlockPos framePos) {
		return getEndPortal(frame -> frame.isFrameBlock(framePos), world, framePos);
	}

	public Frame getEndPortalByTopLeft(BlockPos topLeft) {
		return endPortals.get(topLeft);
	}

	public void addEndPortal(Frame frame) {
		endPortals.put(frame.getTopLeft(), frame);
		markDirty();
	}

	public Frame removeEndPortal(BlockPos portalPos) {
		return removeEndPortal(frame -> frame.isInnerBlock(portalPos), portalPos);
	}

	public Frame removeEndPortalByFrame(BlockPos portalPos) {
		return removeEndPortal(frame -> frame.isFrameBlock(portalPos), portalPos);
	}

	private NetherPortal getNetherPortal(Predicate<Frame> predicate, World world, BlockPos pos) {
		for(NetherPortal portal : netherPortals.values()) {
			final Frame frame = portal.getFrame();

			if(predicate.test(frame)) {
				if(world != null) {
					frame.setWorld(world);
				}

				return portal;
			}
		}

		return null;
	}

	private NetherPortal removeNetherPortal(Predicate<Frame> predicate, BlockPos pos) {
		for(Map.Entry<BlockPos, NetherPortal> entry : netherPortals.entrySet()) {
			final NetherPortal portal = entry.getValue();

			if(predicate.test(portal.getFrame())) {
				netherPortals.remove(entry.getKey());
				markDirty();
				return portal;
			}
		}

		return null;
	}

	private Frame getEndPortal(Predicate<Frame> predicate, World world, BlockPos pos) {
		for(Frame frame : endPortals.values()) {
			if(predicate.test(frame)) {
				if(world != null) {
					frame.setWorld(world);
				}

				return frame;
			}
		}

		return null;
	}

	private Frame removeEndPortal(Predicate<Frame> predicate, BlockPos pos) {
		for(Map.Entry<BlockPos, Frame> entry : endPortals.entrySet()) {
			final Frame frame = entry.getValue();

			if(predicate.test(frame)) {
				endPortals.remove(entry.getKey());
				markDirty();
				return frame;
			}
		}

		return null;
	}

	public static Frame readFrame(NBTTagCompound compound) {
		final int width = compound.getInteger(WIDTH_KEY);

		return width == 0 ? null : new Frame(
				null,
				TYPES[compound.getInteger(FRAME_TYPE_KEY)],
				NBTUtil.getPosFromTag(compound.getCompoundTag(TOP_LEFT_KEY)),
				width,
				compound.getInteger(HEIGHT_KEY)
		);
	}

	public static NBTTagCompound writeFrame(NBTTagCompound compound, Frame frame) {
		if(frame == null) {
			return compound;
		}

		compound.setInteger(FRAME_TYPE_KEY, frame.getType().ordinal());
		compound.setTag(TOP_LEFT_KEY, NBTUtil.createPosTag(frame.getTopLeft()));
		compound.setInteger(WIDTH_KEY, frame.getWidth());
		compound.setInteger(HEIGHT_KEY, frame.getHeight());

		return compound;
	}

	public static RPOSavedData get(World world) {
		final MapStorage storage = world.getPerWorldStorage();
		RPOSavedData instance =
				(RPOSavedData) storage.getOrLoadData(RPOSavedData.class, ID);

		if(instance == null) {
			instance = new RPOSavedData();
			storage.setData(ID, instance);
		}

		return instance;
	}
}
