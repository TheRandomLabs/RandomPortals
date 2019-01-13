package com.therandomlabs.randomportals.advancements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.api.frame.FrameType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class ActivatedNetherPortalTrigger
		implements ICriterionTrigger<ActivatedNetherPortalTrigger.Instance> {
	public static final class Instance extends AbstractCriterionInstance {
		private final FrameType type;
		private final int size;

		public Instance(FrameType type, int size) {
			super(ID);
			this.type = type;
			this.size = size;

			RandomPortals.LOGGER.error(type + " " + size);
		}

		public boolean test(EntityPlayerMP player, FrameType type, int size) {
			RandomPortals.LOGGER.error(type + " " + size);
			return this.type.test(type) && size >= this.size;
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

		public void trigger(EntityPlayerMP player, FrameType type, int size) {
			listeners.stream().
					filter(listener -> listener.getCriterionInstance().test(player, type, size)).
					forEach(listener -> listener.grantCriterion(advancements));
		}
	}

	private static final ResourceLocation ID =
			new ResourceLocation(RandomPortals.MOD_ID, "activated_nether_portal");

	private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addListener(PlayerAdvancements advancements, Listener<Instance> listener) {
		listeners.computeIfAbsent(advancements, Listeners::new).add(listener);
	}

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
		return new Instance(
				FrameType.valueOf(object.get("type").getAsString().toUpperCase(Locale.ROOT)),
				object.get("size").getAsInt()
		);
	}

	public void trigger(EntityPlayerMP player, FrameType type, int size) {
		final Listeners listeners = this.listeners.get(player.getAdvancements());

		if(listeners != null) {
			listeners.trigger(player, type, size);
		}
	}

	public static void register() {
		CriteriaTriggers.register(new ActivatedNetherPortalTrigger());
	}
}
