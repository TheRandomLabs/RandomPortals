package com.therandomlabs.randomportals.api.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

public final class PortalTypeGroup {
	public boolean blacklistAllUndefinedDimensions = true;
	public int[] activationDimensionIDBlacklist = new int[0];

	public int defaultDimensionID;
	//Chance that a zombie pigman spawns each tick is d in n, where d is the difficulty
	//from 0 to 3 and n is the defined spawn rate
	public Map<Integer, EntitySpawns> entitySpawns = new HashMap<>();

	public transient Map<Integer, PortalType> types = new HashMap<>();

	transient String id = "unknown_portal";

	public PortalTypeGroup() {
		this(null);
	}

	public PortalTypeGroup(String id) {
		if (id != null) {
			this.id = id;
		}

		entitySpawns.put(0, new EntitySpawns());
	}

	@Override
	public String toString() {
		return id;
	}

	public void ensureCorrect() {
		entitySpawns.values().forEach(EntitySpawns::ensureCorrect);
	}

	public boolean isValid() {
		return types.containsKey(defaultDimensionID);
	}

	public boolean canActivateInDimension(int dimensionID) {
		if (blacklistAllUndefinedDimensions) {
			if (types.containsKey(dimensionID)) {
				return true;
			}

			for (PortalType type : types.values()) {
				if (type.destination.dimensionID == dimensionID) {
					return true;
				}
			}

			return false;
		}

		return !ArrayUtils.contains(activationDimensionIDBlacklist, dimensionID);
	}

	public PortalType getType(int dimensionID) {
		final PortalType type = types.get(dimensionID);
		return type == null ? getDefaultType() : type;
	}

	public PortalType getDefaultType() {
		return types.get(defaultDimensionID);
	}
}
