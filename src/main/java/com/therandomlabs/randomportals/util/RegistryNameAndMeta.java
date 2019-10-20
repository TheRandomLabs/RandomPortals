package com.therandomlabs.randomportals.util;

import net.minecraftforge.oredict.OreDictionary;

public final class RegistryNameAndMeta {
	public final String registryName;
	public final int meta;

	public RegistryNameAndMeta(String registryName, int meta) {
		this.registryName = registryName;
		this.meta = meta;
	}

	@Override
	public int hashCode() {
		return registryName.hashCode() * meta;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object instanceof RegistryNameAndMeta) {
			final RegistryNameAndMeta registryNameAndMeta = (RegistryNameAndMeta) object;
			return registryName.equals(registryNameAndMeta.registryName) &&
					(meta == OreDictionary.WILDCARD_VALUE || meta == registryNameAndMeta.meta);
		}

		return false;
	}
}
