package com.therandomlabs.verticalendportals.world.storage;

import java.util.ArrayList;
import java.util.List;
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

	private final List<Portal> portals = new ArrayList<>();

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

			portals.add(new Portal(type, new Frame(null, frameType, topLeft, width, height)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		final NBTTagList tagList = new NBTTagList();

		for(Portal portal : portals) {
			final NBTTagCompound compound = new NBTTagCompound();

			compound.setString(PORTAL_TYPE_KEY, portal.type);
			compound.setInteger(FRAME_TYPE_KEY, portal.frame.getType().ordinal());
			compound.setTag(TOP_LEFT_KEY, NBTUtil.createPosTag(portal.frame.getTopLeft()));
			compound.setInteger(WIDTH_KEY, portal.frame.getWidth());
			compound.setInteger(HEIGHT_KEY, portal.frame.getHeight());

			tagList.appendTag(tagList);
		}

		nbt.setTag(TAG_KEY, tagList);
		return nbt;
	}

	public List<Portal> getPortals() {
		return portals;
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
