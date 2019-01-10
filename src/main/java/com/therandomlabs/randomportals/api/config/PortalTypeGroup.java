package com.therandomlabs.randomportals.api.config;

import java.util.HashMap;
import java.util.Map;

public final class PortalTypeGroup {
	public boolean useActivationDimensionsBlacklist = true;
	public int[] activationDimensionsBlacklist = new int[0];

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

	public PortalType getType(int dimensionID) {
		final PortalType type = types.get(dimensionID);
		return type == null ? types.get(defaultDimensionID) : type;
	}
}
