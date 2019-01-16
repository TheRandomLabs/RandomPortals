package com.therandomlabs.randomportals.api.config;

import java.util.ArrayList;
import java.util.List;
import com.therandomlabs.randomportals.util.RegistryNameAndMeta;
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

	public List<PortalActivator> activators = new ArrayList<>();

	public ConsumeBehavior activatorConsumeBehavior = ConsumeBehavior.CONSUME;
	public boolean spawnFireBeforeActivating = true;

	public String[] activationSounds = {
			"minecraft:creativetab.flintandsteel.use"
	};

	private transient SoundEvent[] activationSoundEvents;

	@SuppressWarnings("Duplicates")
	public void ensureCorrect() {
		final List<RegistryNameAndMeta> checkedItems = new ArrayList<>();

		for(int i = 0; i < activators.size(); i++) {
			final PortalActivator activator = activators.get(i);
			final RegistryNameAndMeta registryNameAndMeta = new RegistryNameAndMeta(
					activator.registryName, activator.meta
			);

			if(!activator.isValid() || checkedItems.contains(registryNameAndMeta)) {
				activators.remove(i--);
				continue;
			}

			checkedItems.add(registryNameAndMeta);
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
		for(PortalActivator activator : activators) {
			if(activator.test(stack)) {
				return true;
			}
		}

		return false;
	}
}
