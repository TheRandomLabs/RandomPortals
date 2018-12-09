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
	public static final class Portal {
		private final NetherPortalType type;
		private final Frame frame;
		private final boolean userCreated;

		public Portal(NetherPortalType type, Frame frame, boolean userCreated) {
			this.type = type;
			this.frame = frame;
			this.userCreated = userCreated;
		}

		@Override
		public String toString() {
			return "Portal[type=" + type.getName() + ",frame=" + frame + ",userCreated=" +
					userCreated + "]";
		}

		public NetherPortalType getType() {
			return type;
		}

		public Frame getFrame() {
			return frame;
		}

		public boolean isUserCreated() {
			return userCreated;
		}
	}

	public static final String ID = "nether_portals";

	public static final String USER_CREATED_PORTALS_KEY = "UserCreatedPortals";
	public static final String GENERATED_PORTALS_KEY = "GeneratedPortals";

	public static final String PORTAL_TYPE_KEY = "PortalType";
	public static final String FRAME_TYPE_KEY = "FrameType";
	public static final String TOP_LEFT_KEY = "TopLeft";
	public static final String WIDTH_KEY = "Width";
	public static final String HEIGHT_KEY = "Height";

	private static final FrameType[] TYPES = FrameType.values();

	private final Map<BlockPos, Portal> userCreatedPortals = new HashMap<>();
	private final Map<BlockPos, Portal> generatedPortals = new HashMap<>();
	private final Map<BlockPos, Portal> portalCache = new HashMap<>();
	private final Map<BlockPos, Portal> portalFrameCache = new HashMap<>();

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

	public Map<BlockPos, Portal> getUserCreatedPortals() {
		return userCreatedPortals;
	}

	public Map<BlockPos, Portal> getUserCreatedPortals(World world) {
		return getPortals(userCreatedPortals, world);
	}

	public Map<BlockPos, Portal> getGeneratedPortals() {
		return generatedPortals;
	}

	public Map<BlockPos, Portal> getGeneratedPortals(World world) {
		return getPortals(generatedPortals, world);
	}

	public Portal getPortal(BlockPos portalPos) {
		return getPortal(null, portalPos);
	}

	public Portal getPortal(World world, BlockPos portalPos) {
		return getPortal(portalCache, frame -> frame.isInnerBlock(portalPos), world, portalPos);
	}

	public Portal getPortalByFrame(BlockPos portalPos) {
		return getPortalByFrame(null, portalPos);
	}

	public Portal getPortalByFrame(World world, BlockPos framePos) {
		return getPortal(portalFrameCache, frame -> frame.isFrameBlock(framePos), world, framePos);
	}

	public Portal removePortal(BlockPos portalPos) {
		return removePortal(frame -> frame.isInnerBlock(portalPos), portalPos);
	}

	public Portal removePortalByFrame(BlockPos portalPos) {
		return removePortal(frame -> frame.isFrameBlock(portalPos), portalPos);
	}

	public Portal getPortalByTopLeft(BlockPos topLeft) {
		final Portal portal = userCreatedPortals.get(topLeft);
		return portal == null ? generatedPortals.get(topLeft) : portal;
	}

	public void addPortal(Portal portal) {
		if(portal.userCreated) {
			addPortal(userCreatedPortals, portal);
		} else {
			addPortal(generatedPortals, portal);
		}
	}

	private void addPortal(Map<BlockPos, Portal> portals, Portal portal) {
		portals.put(portal.frame.getTopLeft(), portal);
		markDirty();
	}

	private Portal removePortal(Map<BlockPos, Portal> portals, Predicate<Frame> predicate,
			BlockPos pos) {
		for(Map.Entry<BlockPos, Portal> entry : portals.entrySet()) {
			final Portal portal = entry.getValue();

			if(predicate.test(portal.frame)) {
				portals.remove(entry.getKey());

				final List<BlockPos> toRemove = new ArrayList<>();

				for(Map.Entry<BlockPos, Portal> cache : portalCache.entrySet()) {
					toRemove.add(cache.getKey());
				}

				portalCache.keySet().removeAll(toRemove);

				markDirty();
				return portal;
			}
		}

		return null;
	}

	private Portal removePortal(Predicate<Frame> predicate, BlockPos portalPos) {
		final Portal portal = removePortal(userCreatedPortals, predicate, portalPos);
		return portal == null ? removePortal(generatedPortals, predicate, portalPos) : portal;
	}

	private void read(NBTTagList list, Map<BlockPos, Portal> portals, boolean userCreated) {
		for(NBTBase tag : list) {
			final NBTTagCompound compound = (NBTTagCompound) tag;

			final NetherPortalType type =
					NetherPortalTypes.get(compound.getString(PORTAL_TYPE_KEY));
			final FrameType frameType = TYPES[compound.getInteger(FRAME_TYPE_KEY)];
			final BlockPos topLeft = NBTUtil.getPosFromTag(compound.getCompoundTag(TOP_LEFT_KEY));
			final int width = compound.getInteger(WIDTH_KEY);
			final int height = compound.getInteger(HEIGHT_KEY);

			portals.put(topLeft, new Portal(
					type, new Frame(null, frameType, topLeft, width, height), userCreated
			));
		}
	}

	private NBTTagList write(Map<BlockPos, Portal> portals, NBTTagList list) {
		for(Portal portal : portals.values()) {
			final NBTTagCompound compound = new NBTTagCompound();

			compound.setString(PORTAL_TYPE_KEY, portal.type.getName());
			compound.setInteger(FRAME_TYPE_KEY, portal.frame.getType().ordinal());
			compound.setTag(TOP_LEFT_KEY, NBTUtil.createPosTag(portal.frame.getTopLeft()));
			compound.setInteger(WIDTH_KEY, portal.frame.getWidth());
			compound.setInteger(HEIGHT_KEY, portal.frame.getHeight());

			list.appendTag(compound);
		}

		return list;
	}

	private Map<BlockPos, Portal> getPortals(Map<BlockPos, Portal> portals, World world) {
		if(world != null) {
			for(Portal portal : portals.values()) {
				portal.frame.setWorld(world);
			}
		}

		return portals;
	}

	private Portal getPortal(Map<BlockPos, Portal> portalCache, Predicate<Frame> predicate,
			World world, BlockPos portalPos) {
		Portal portal = portalCache.get(portalPos);

		if(portal != null) {
			if(world != null) {
				portal.frame.setWorld(world);
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

	private Portal actuallyGetPortal(Map<BlockPos, Portal> portals, Predicate<Frame> predicate,
			World world, BlockPos pos) {
		for(Portal portal : portals.values()) {
			if(predicate.test(portal.frame)) {
				if(world != null) {
					portal.frame.setWorld(world);
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
