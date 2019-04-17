package com.therandomlabs.randomportals.api.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

public final class EntitySpawns {
	public int rate = 2000;
	public Map<String, SpawnRate> rates = new HashMap<>();

	public transient double totalWeight;

	public EntitySpawns() {
		rates.put("minecraft:zombie_pigman", new SpawnRate());
	}

	public void ensureCorrect() {
		if(rate < 3) {
			rate = 2000;
		}

		final Map<String, SpawnRate> newRates = new HashMap<>(rates.size());
		final ResourceLocation[] entityNames =
				EntityList.getEntityNameList().toArray(new ResourceLocation[0]);

		totalWeight = 0.0;

		final List<String> registryNames = new ArrayList<>(rates.keySet());
		final List<SpawnRate> spawnRates = new ArrayList<>(rates.values());

		for(int i = 0; i < registryNames.size(); i++) {
			final int index = ArrayUtils.indexOf(
					entityNames, new ResourceLocation(registryNames.get(i))
			);

			if(index != ArrayUtils.INDEX_NOT_FOUND) {
				final String key = entityNames[index].toString();

				final SpawnRate spawnRate = spawnRates.get(i);
				spawnRate.key = key;

				totalWeight += spawnRate.weight;

				newRates.put(key, spawnRate);
			}
		}

		rates = newRates;
	}

	public SpawnRate getRandom(Random random) {
		final double result = random.nextDouble() * totalWeight;
		double weight = 0.0;

		for(SpawnRate spawnRate : rates.values()) {
			weight += spawnRate.weight;

			if(weight >= result) {
				return spawnRate;
			}
		}

		return null;
	}
}
