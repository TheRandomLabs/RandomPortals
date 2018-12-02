package com.therandomlabs.verticalendportals.world.storage;

import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.config.NetherPortalType;
import com.therandomlabs.verticalendportals.config.NetherPortalTypes;
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
	public static final String TAG_KEY = "Portals";
	public static final String PORTAL_TYPE_KEY = "PortalType";
	public static final String FRAME_TYPE_KEY = "FrameType";
	public static final String TOP_LEFT_KEY = "TopLeft";
	public static final String WIDTH_KEY = "Width";
	public static final String HEIGHT_KEY = "Height";

	private static final FrameType[] TYPES = FrameType.values();

	private final Map<BlockPos, Portal> portals = new HashMap<>();
	private final Map<BlockPos, Portal> portalCache = new HashMap<>();

	public static final class Portal {
		private final String type;
		private final Frame frame;

		public Portal(String type, Frame frame) {
			this.type = type;
			this.frame = frame;
		}

		public NetherPortalType getType() {
			return NetherPortalTypes.get(type);
		}

		public Frame getFrame() {
			return frame;
		}
	}

	public NetherPortalSavedData() {
		super(ID);
	}

	public NetherPortalSavedData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		portals.clear();

		final NBTTagList list = nbt.getTagList(TAG_KEY, Constants.NBT.TAG_COMPOUND);

		for(NBTBase tag : list) {
			final NBTTagCompound compound = (NBTTagCompound) tag;

			final String type = compound.getString(PORTAL_TYPE_KEY);
			final FrameType frameType = TYPES[compound.getInteger(FRAME_TYPE_KEY)];
			final BlockPos topLeft = NBTUtil.getPosFromTag(compound.getCompoundTag(TOP_LEFT_KEY));
			final int width = compound.getInteger(WIDTH_KEY);
			final int height = compound.getInteger(HEIGHT_KEY);

			portals.put(
					topLeft, new Portal(type, new Frame(null, frameType, topLeft, width, height))
			);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		final NBTTagList tagList = new NBTTagList();

		for(Portal portal : portals.values()) {
			final NBTTagCompound compound = new NBTTagCompound();

			compound.setString(PORTAL_TYPE_KEY, portal.type);
			compound.setInteger(FRAME_TYPE_KEY, portal.frame.getType().ordinal());
			compound.setTag(TOP_LEFT_KEY, NBTUtil.createPosTag(portal.frame.getTopLeft()));
			compound.setInteger(WIDTH_KEY, portal.frame.getWidth());
			compound.setInteger(HEIGHT_KEY, portal.frame.getHeight());

			tagList.appendTag(compound);
		}

		nbt.setTag(TAG_KEY, tagList);
		return nbt;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		portalCache.clear();
	}

	public Map<BlockPos, Portal> getPortals() {
		return portals;
	}

	public Portal getPortal(BlockPos portalPos) {
		final Portal cachedPortal = portalCache.get(portalPos);

		if(cachedPortal != null) {
			return cachedPortal;
		}

		for(Portal portal : portals.values()) {
			if(portal.frame.isInnerBlock(portalPos)) {
				portals.put(portalPos, portal);
				return portal;
			}
		}

		return null;
	}

	public Portal getPortalByTopLeft(BlockPos topLeft) {
		return portals.get(topLeft);
	}

	public void addPortal(Portal portal) {
		portals.put(portal.frame.getTopLeft(), portal);
		markDirty();
	}

	public static NetherPortalSavedData get(World world) {
		final MapStorage storage = world.getMapStorage();
		NetherPortalSavedData instance =
				(NetherPortalSavedData) storage.getOrLoadData(NetherPortalSavedData.class, ID);

		if(instance == null) {
			instance = new NetherPortalSavedData();
			storage.setData(ID, instance);
		}

		return instance;
	}
}
