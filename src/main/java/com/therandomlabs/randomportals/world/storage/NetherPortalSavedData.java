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

public class NetherPortalSavedData extends WorldSavedData {
	public static final String ID = "nether_portals";

	public static final String PORTALS_KEY = "Portals";

	public static final String PORTAL_TYPE_KEY = "PortalType";
	public static final String FRAME_TYPE_KEY = "FrameType";
	public static final String TOP_LEFT_KEY = "TopLeft";
	public static final String WIDTH_KEY = "Width";
	public static final String HEIGHT_KEY = "Height";

	public static final String GENERATED_PORTAL_FRAMES_KEY = "GeneratedPortalFrames";

	private static final FrameType[] TYPES = FrameType.values();

	private final Map<BlockPos, NetherPortal> portals = new HashMap<>();
	private final Map<String, Set<BlockPos>> generatedPortalFrames = new HashMap<>();

	public NetherPortalSavedData() {
		super(ID);
	}

	public NetherPortalSavedData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		portals.clear();
		generatedPortalFrames.clear();

		for(NBTBase tag : nbt.getTagList(PORTALS_KEY, Constants.NBT.TAG_COMPOUND)) {
			final NBTTagCompound compound = (NBTTagCompound) tag;

			final NetherPortalType type =
					NetherPortalTypes.get(compound.getString(PORTAL_TYPE_KEY));
			final FrameType frameType = TYPES[compound.getInteger(FRAME_TYPE_KEY)];
			final BlockPos topLeft = NBTUtil.getPosFromTag(compound.getCompoundTag(TOP_LEFT_KEY));
			final int width = compound.getInteger(WIDTH_KEY);
			final int height = compound.getInteger(HEIGHT_KEY);

			portals.put(topLeft, new NetherPortal(
					new Frame(null, frameType, topLeft, width, height), type
			));
		}

		final NBTTagCompound compound = nbt.getCompoundTag(GENERATED_PORTAL_FRAMES_KEY);

		for(String typeName : compound.getKeySet()) {
			final NBTTagList list = compound.getTagList(typeName, Constants.NBT.TAG_COMPOUND);
			final Set<BlockPos> positions = new HashSet<>(list.tagCount());

			for(NBTBase tag : list) {
				positions.add(NBTUtil.getPosFromTag((NBTTagCompound) tag));
			}

			generatedPortalFrames.merge(
					//If the type name is invalid, the default type's name is returned
					NetherPortalTypes.get(typeName).getName(),
					positions,
					(a, b) -> {
						a.addAll(b);
						return a;
					}
			);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		final NBTTagList portalList = new NBTTagList();

		for(NetherPortal portal : portals.values()) {
			final NBTTagCompound compound = new NBTTagCompound();
			final Frame frame = portal.getFrame();

			compound.setString(PORTAL_TYPE_KEY, portal.getType().getName());
			compound.setInteger(FRAME_TYPE_KEY, frame.getType().ordinal());
			compound.setTag(TOP_LEFT_KEY, NBTUtil.createPosTag(frame.getTopLeft()));
			compound.setInteger(WIDTH_KEY, frame.getWidth());
			compound.setInteger(HEIGHT_KEY, frame.getHeight());

			portalList.appendTag(compound);
		}

		nbt.setTag(PORTALS_KEY, portalList);

		final NBTTagCompound generatedPortalFramesTag = new NBTTagCompound();

		for(Map.Entry<String, Set<BlockPos>> entry : generatedPortalFrames.entrySet()) {
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

		nbt.setTag(GENERATED_PORTAL_FRAMES_KEY, generatedPortalFramesTag);

		return nbt;
	}

	public Map<BlockPos, NetherPortal> getPortals() {
		return portals;
	}

	public Map<BlockPos, NetherPortal> getUserCreatedPortals(World world) {
		if(world != null) {
			for(NetherPortal portal : portals.values()) {
				portal.getFrame().setWorld(world);
			}
		}

		return portals;
	}

	public NetherPortal getPortal(BlockPos portalPos) {
		return getPortal(null, portalPos);
	}

	public NetherPortal getPortal(World world, BlockPos portalPos) {
		return getPortal(frame -> frame.isInnerBlock(portalPos), world, portalPos);
	}

	public NetherPortal getPortalByFrame(BlockPos portalPos) {
		return getPortalByFrame(null, portalPos);
	}

	public NetherPortal getPortalByFrame(World world, BlockPos framePos) {
		return getPortal(frame -> frame.isFrameBlock(framePos), world, framePos);
	}

	public NetherPortal removePortal(BlockPos portalPos) {
		return removePortal(frame -> frame.isInnerBlock(portalPos), portalPos);
	}

	public NetherPortal removePortalByFrame(BlockPos portalPos) {
		return removePortal(frame -> frame.isFrameBlock(portalPos), portalPos);
	}

	public NetherPortal getPortalByTopLeft(BlockPos topLeft) {
		return portals.get(topLeft);
	}

	public NetherPortalType getGeneratedPortalType(BlockPos framePos) {
		for(Map.Entry<String, Set<BlockPos>> entry : generatedPortalFrames.entrySet()) {
			if(entry.getValue().contains(framePos)) {
				return NetherPortalTypes.get(entry.getKey());
			}
		}

		return null;
	}

	public void removeGeneratedPortalFramePos(String typeName, BlockPos framePos) {
		final Set<BlockPos> positions = generatedPortalFrames.get(typeName);

		if(positions != null) {
			markDirty();
		}
	}

	public NetherPortal addPortal(Frame frame, NetherPortalType type, boolean userCreated) {
		final NetherPortal portal = new NetherPortal(frame, type);
		addPortal(portal, userCreated);
		return portal;
	}

	public void addPortal(NetherPortal portal, boolean userCreated) {
		final Frame frame = portal.getFrame();

		portals.put(frame.getTopLeft(), portal);

		if(!userCreated) {
			generatedPortalFrames.merge(
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

	private NetherPortal getPortal(Predicate<Frame> predicate, World world, BlockPos pos) {
		for(NetherPortal portal : portals.values()) {
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

	private NetherPortal removePortal(Predicate<Frame> predicate, BlockPos pos) {
		for(Map.Entry<BlockPos, NetherPortal> entry : portals.entrySet()) {
			final NetherPortal portal = entry.getValue();

			if(predicate.test(portal.getFrame())) {
				portals.remove(entry.getKey());
				markDirty();
				return portal;
			}
		}

		return null;
	}

	public static NetherPortalSavedData get(World world) {
		final MapStorage storage = world.getPerWorldStorage();
		NetherPortalSavedData instance =
				(NetherPortalSavedData) storage.getOrLoadData(NetherPortalSavedData.class, ID);

		if(instance == null) {
			instance = new NetherPortalSavedData();
			storage.setData(ID, instance);
		}

		return instance;
	}
}
