package com.therandomlabs.randomportals.api.config;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

public final class SpawnRate {
	public String nbt = "{}";
	public double weight = 100.0;

	public transient String key;

	public void ensureCorrect() {
		try {
			JsonToNBT.getTagFromJson(nbt);
		} catch(NBTException ex) {
			nbt = "{}";
		}

		if(weight <= 0.0) {
			weight = 100.0;
		}
	}
}
