package com.therandomlabs.verticalendportals.world.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
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

	public static final String USER_CREATED_PORTALS_KEY = "UserCreatedPortals";
	public static final String GENERATED_PORTALS_KEY = "GeneratedPortals";

	public static final String PORTAL_TYPE_KEY = "PortalType";
	public static final String FRAME_TYPE_KEY = "FrameType";
	public static final String TOP_LEFT_KEY = "TopLeft";
	public static final String WIDTH_KEY = "Width";
	public static final String HEIGHT_KEY = "Height";

	private static final FrameType[] TYPES = FrameType.values();

	private final Map<BlockPos, PortalData> userCreatedPortals = new HashMap<>();
	private final Map<BlockPos, PortalData> generatedPortals = new HashMap<>();
	private final Map<BlockPos, PortalData> portalCache = new HashMap<>();
	private final Map<BlockPos, PortalData> portalFrameCache = new HashMap<>();

	public NetherPortalSavedData() {
		super(ID);
	}

	public NetherPortalSavedData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		userCreatedPortals.clear();
		generatedPortals.clear();
		portalCache.clear();

		read(
				nbt.getTagList(USER_CREATED_PORTALS_KEY, Constants.NBT.TAG_COMPOUND),
				userCreatedPortals, true
		);

		read(
				nbt.getTagList(GENERATED_PORTALS_KEY, Constants.NBT.TAG_COMPOUND),
				generatedPortals, false
		);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag(USER_CREATED_PORTALS_KEY, write(userCreatedPortals, new NBTTagList()));
		nbt.setTag(GENERATED_PORTALS_KEY, write(generatedPortals, new NBTTagList()));
		return nbt;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		portalCache.clear();
	}

	public Map<BlockPos, PortalData> getUserCreatedPortals() {
		return userCreatedPortals;
	}

	public Map<BlockPos, PortalData> getUserCreatedPortals(World world) {
		return getPortals(userCreatedPortals, world);
	}

	public Map<BlockPos, PortalData> getGeneratedPortals() {
		return generatedPortals;
	}

	public Map<BlockPos, PortalData> getGeneratedPortals(World world) {
		return getPortals(generatedPortals, world);
	}

	public PortalData getPortal(BlockPos portalPos) {
		return getPortal(null, portalPos);
	}

	public PortalData getPortal(World world, BlockPos portalPos) {
		return getPortal(portalCache, frame -> frame.isInnerBlock(portalPos), world, portalPos);
	}

	public PortalData getPortalByFrame(BlockPos portalPos) {
		return getPortalByFrame(null, portalPos);
	}

	public PortalData getPortalByFrame(World world, BlockPos framePos) {
		return getPortal(portalFrameCache, frame -> frame.isFrameBlock(framePos), world, framePos);
	}

	public PortalData removePortal(BlockPos portalPos) {
		return removePortal(frame -> frame.isInnerBlock(portalPos), portalPos);
	}

	public PortalData removePortalByFrame(BlockPos portalPos) {
		return removePortal(frame -> frame.isFrameBlock(portalPos), portalPos);
	}

	public PortalData getPortalByTopLeft(BlockPos topLeft) {
		final PortalData portal = userCreatedPortals.get(topLeft);
		return portal == null ? generatedPortals.get(topLeft) : portal;
	}

	public void addPortal(NetherPortalType type, Frame frame, boolean userCreated) {
		addPortal(new PortalData(type, frame, userCreated));
	}

	public void addPortal(PortalData portal) {
		if(portal.isUserCreated()) {
			addPortal(userCreatedPortals, portal);
		} else {
			addPortal(generatedPortals, portal);
		}
	}

	private void addPortal(Map<BlockPos, PortalData> portals, PortalData portal) {
		portals.put(portal.getFrame().getTopLeft(), portal);
		markDirty();
	}

	private PortalData removePortal(Map<BlockPos, PortalData> portals, Predicate<Frame> predicate,
			BlockPos pos) {
		for(Map.Entry<BlockPos, PortalData> entry : portals.entrySet()) {
			final PortalData portal = entry.getValue();

			if(predicate.test(portal.getFrame())) {
				portals.remove(entry.getKey());

				final List<BlockPos> toRemove = new ArrayList<>();

				for(Map.Entry<BlockPos, PortalData> cache : portalCache.entrySet()) {
					toRemove.add(cache.getKey());
				}

				portalCache.keySet().removeAll(toRemove);

				markDirty();
				return portal;
			}
		}

		return null;
	}

	private PortalData removePortal(Predicate<Frame> predicate, BlockPos portalPos) {
		final PortalData portal = removePortal(userCreatedPortals, predicate, portalPos);
		return portal == null ? removePortal(generatedPortals, predicate, portalPos) : portal;
	}

	private void read(NBTTagList list, Map<BlockPos, PortalData> portals, boolean userCreated) {
		for(NBTBase tag : list) {
			final NBTTagCompound compound = (NBTTagCompound) tag;

			final NetherPortalType type =
					NetherPortalTypes.get(compound.getString(PORTAL_TYPE_KEY));
			final FrameType frameType = TYPES[compound.getInteger(FRAME_TYPE_KEY)];
			final BlockPos topLeft = NBTUtil.getPosFromTag(compound.getCompoundTag(TOP_LEFT_KEY));
			final int width = compound.getInteger(WIDTH_KEY);
			final int height = compound.getInteger(HEIGHT_KEY);

			portals.put(topLeft, new PortalData(
					type, new Frame(null, frameType, topLeft, width, height), userCreated
			));
		}
	}

	private NBTTagList write(Map<BlockPos, PortalData> portals, NBTTagList list) {
		for(PortalData portal : portals.values()) {
			final NBTTagCompound compound = new NBTTagCompound();
			final Frame frame = portal.getFrame();

			compound.setString(PORTAL_TYPE_KEY, portal.getType().getName());
			compound.setInteger(FRAME_TYPE_KEY, frame.getType().ordinal());
			compound.setTag(TOP_LEFT_KEY, NBTUtil.createPosTag(frame.getTopLeft()));
			compound.setInteger(WIDTH_KEY, frame.getWidth());
			compound.setInteger(HEIGHT_KEY, frame.getHeight());

			list.appendTag(compound);
		}

		return list;
	}

	private Map<BlockPos, PortalData> getPortals(Map<BlockPos, PortalData> portals, World world) {
		if(world != null) {
			for(PortalData portal : portals.values()) {
				portal.getFrame().setWorld(world);
			}
		}

		return portals;
	}

	private PortalData getPortal(Map<BlockPos, PortalData> portalCache, Predicate<Frame> predicate,
			World world, BlockPos portalPos) {
		PortalData portal = portalCache.get(portalPos);

		if(portal != null) {
			if(world != null) {
				portal.getFrame().setWorld(world);
			}

			return portal;
		}

		portal = actuallyGetPortal(userCreatedPortals, predicate, world, portalPos);

		if(portal != null) {
			portalCache.put(portalPos, portal);
		}

		portal = actuallyGetPortal(userCreatedPortals, predicate, world, portalPos);
		portalCache.put(portalPos, portal);
		return portal;
	}

	private PortalData actuallyGetPortal(Map<BlockPos, PortalData> portals,
			Predicate<Frame> predicate, World world, BlockPos pos) {
		for(PortalData portal : portals.values()) {
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
