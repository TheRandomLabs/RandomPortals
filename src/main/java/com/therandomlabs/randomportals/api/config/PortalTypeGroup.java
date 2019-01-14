package com.therandomlabs.randomportals.api.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

public final class PortalTypeGroup {
	public boolean blacklistAllUndefinedActivationDimensions;
	public int[] activationDimensionIDBlacklist = new int[0];

	public int defaultDimensionID;

	public transient Map<Integer, PortalType> types = new HashMap<>();

	transient String id = "unknown_portal";

	public PortalTypeGroup() {}

	public PortalTypeGroup(String id) {
		if(id != null) {
			this.id = id;
		}
	}

	@Override
	public String toString() {
		return id;
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
