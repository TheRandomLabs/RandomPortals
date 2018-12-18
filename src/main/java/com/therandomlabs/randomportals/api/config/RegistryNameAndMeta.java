package com.therandomlabs.randomportals.api.config;

import net.minecraftforge.oredict.OreDictionary;

final class RegistryNameAndMeta {
	final String registryName;
	final int meta;

	RegistryNameAndMeta(String registryName, int meta) {
		this.registryName = registryName;
		this.meta = meta;
	}

	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}

		if(object instanceof RegistryNameAndMeta) {
			final RegistryNameAndMeta registryNameAndMeta = (RegistryNameAndMeta) object;
			return registryName.equals(registryNameAndMeta.registryName) &&
					(meta == OreDictionary.WILDCARD_VALUE || meta == registryNameAndMeta.meta);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return registryName.hashCode() * meta;
	}
}
