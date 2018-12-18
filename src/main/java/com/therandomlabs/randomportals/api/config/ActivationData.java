package com.therandomlabs.randomportals.api.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public final class ActivationData {
	public enum ConsumeBehavior {
		CONSUME,
		DAMAGE,
		DO_NOTHING
	}

	public boolean canBeActivatedByFire = true;

	public List<FrameActivator> activators = new ArrayList<>();

	public ConsumeBehavior activatorConsumeBehavior = ConsumeBehavior.CONSUME;
	public boolean spawnFireBeforeActivating = true;

	public String[] activationSounds = {
			"minecraft:item.flintandsteel.use"
	};

	private transient SoundEvent[] activationSoundEvents;

	@SuppressWarnings("Duplicates")
	public void ensureCorrect() {
		final List<RegistryNameAndMeta> items = new ArrayList<>();

		for(int i = 0; i < activators.size(); i++) {
			final FrameActivator activator = activators.get(i);
			final RegistryNameAndMeta registryNameAndMeta =
					new RegistryNameAndMeta(activator.registryName, activator.meta);

			if(!activator.isValid() || items.contains(registryNameAndMeta)) {
				activators.remove(i--);
				continue;
			}

			items.add(registryNameAndMeta);
		}

		getActivationSoundEvents();
	}

	public SoundEvent[] getActivationSoundEvents() {
		if(activationSoundEvents != null) {
			return activationSoundEvents;
		}

		final List<String> sounds = new ArrayList<>(activationSounds.length);
		final List<SoundEvent> soundEvents = new ArrayList<>(activationSounds.length);

		for(String activationSound : activationSounds) {
			final SoundEvent soundEvent =
					SoundEvent.REGISTRY.getObject(new ResourceLocation(activationSound));

			if(soundEvent != null) {
				final String registryName = soundEvent.getRegistryName().toString();

				if(!sounds.contains(registryName)) {
					sounds.add(registryName);
					soundEvents.add(soundEvent);
				}
			}
		}

		activationSounds = sounds.toArray(new String[0]);
		activationSoundEvents = soundEvents.toArray(new SoundEvent[0]);

		return activationSoundEvents;
	}

	public boolean test(ItemStack stack) {
		for(FrameActivator activator : activators) {
			if(activator.test(stack)) {
				return true;
			}
		}

		return false;
	}
}
