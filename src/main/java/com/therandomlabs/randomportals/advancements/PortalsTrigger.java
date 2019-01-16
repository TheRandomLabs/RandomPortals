package com.therandomlabs.randomportals.advancements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.therandomlabs.randomportals.RandomPortals;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class PortalsTrigger implements ICriterionTrigger<PortalsTrigger.Instance> {
	public static final class Instance extends AbstractCriterionInstance {
		public Instance() {
			super(ID);
		}
	}

	static final class Listeners {
		private final PlayerAdvancements advancements;
		private final Set<Listener<Instance>> listeners = new HashSet<>();

		public Listeners(PlayerAdvancements advancements) {
			this.advancements = advancements;
		}

		public boolean isEmpty() {
			return listeners.isEmpty();
		}

		public void add(Listener<Instance> listener) {
			listeners.add(listener);
		}

		public void remove(Listener<Instance> listener) {
			listeners.remove(listener);
		}

		public void trigger() {
			listeners.forEach(listener -> listener.grantCriterion(advancements));
		}
	}

	private static final ResourceLocation ID =
			new ResourceLocation(RandomPortals.MOD_ID, "portals");

	private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addListener(PlayerAdvancements advancements, Listener<Instance> listener) {
		listeners.computeIfAbsent(advancements, Listeners::new).add(listener);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void removeListener(PlayerAdvancements advancements, Listener<Instance> listener) {
		final Listeners listeners = this.listeners.get(advancements);

		if(listeners != null) {
			listeners.remove(listener);

			if(listeners.isEmpty()) {
				this.listeners.remove(advancements);
			}
		}
	}

	@Override
	public void removeAllListeners(PlayerAdvancements advancements) {
		listeners.remove(advancements);
	}

	@Override
	public Instance deserializeInstance(JsonObject object, JsonDeserializationContext context) {
		return new Instance();
	}

	public void trigger(EntityPlayerMP player) {
		final Listeners listeners = this.listeners.get(player.getAdvancements());

		if(listeners != null) {
			listeners.trigger();
		}
	}
}
