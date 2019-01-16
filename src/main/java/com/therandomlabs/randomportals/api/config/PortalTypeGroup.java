package com.therandomlabs.randomportals.api.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

public final class PortalTypeGroup {
	public boolean blacklistAllUndefinedActivationDimensions;
	public int[] activationDimensionIDBlacklist = new int[0];

	public int defaultDimensionID;
	//Chance that a zombie pigman spawns each tick is d in n, where d is the difficulty
	//from 0 to 3 and n is the defined spawn rate
	public Map<Integer, Integer> zombiePigmanSpawnRates = new HashMap<>();

	public transient Map<Integer, PortalType> types = new HashMap<>();

	transient String id = "unknown_portal";

	public PortalTypeGroup() {
		this(null);
	}

	public PortalTypeGroup(String id) {
		if(id != null) {
			this.id = id;
		}

		zombiePigmanSpawnRates.put(0, 2000);
	}

	@Override
	public String toString() {
		return id;
	}

	public void ensureCorrect() {
		final List<Integer> toRemove = new ArrayList<>();

		for(Map.Entry<Integer, Integer> entry : zombiePigmanSpawnRates.entrySet()) {
			if(entry.getValue() < 1) {
				toRemove.add(entry.getKey());
			}
		}

		toRemove.forEach(zombiePigmanSpawnRates::remove);
	}

	public boolean isValid() {
		return types.containsKey(defaultDimensionID);
	}

	public boolean testActivationDimensionID(int dimensionID) {
		if(blacklistAllUndefinedActivationDimensions) {
			return types.containsKey(dimensionID);
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
