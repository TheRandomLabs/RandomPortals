package com.therandomlabs.randomportals.advancements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public abstract class RPOCriterionTrigger<I extends ICriterionInstance>
		implements ICriterionTrigger<I> {
	public class ListenerContainer {
		private final PlayerAdvancements advancements;
		private final Set<Listener<I>> listeners = new HashSet<>();

		public ListenerContainer(PlayerAdvancements advancements) {
			this.advancements = advancements;
		}

		public boolean isEmpty() {
			return listeners.isEmpty();
		}

		public void add(Listener<I> listener) {
			listeners.add(listener);
		}

		public void remove(Listener<I> listener) {
			listeners.remove(listener);
		}

		public void trigger(Predicate<I> predicate) {
			listeners.stream().
					filter(listener -> predicate.test(listener.getCriterionInstance())).
					collect(Collectors.toList()). //Avoid ConcurrentModificationException
					forEach(listener -> listener.grantCriterion(advancements));
		}
	}

	private final ResourceLocation id;
	private final Map<PlayerAdvancements, ListenerContainer> listeners = new HashMap<>();

	public RPOCriterionTrigger(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public void addListener(PlayerAdvancements advancements, Listener<I> listener) {
		listeners.computeIfAbsent(advancements, ListenerContainer::new).add(listener);
	}

	@Override
	public void removeListener(PlayerAdvancements advancements, Listener<I> listener) {
		final ListenerContainer container = listeners.get(advancements);

		if (container != null) {
			container.remove(listener);

			if (listeners.isEmpty()) {
				listeners.remove(advancements);
			}
		}
	}

	@Override
	public void removeAllListeners(PlayerAdvancements advancements) {
		listeners.remove(advancements);
	}

	protected void trigger(EntityPlayerMP player, Predicate<I> predicate) {
		final ListenerContainer container = listeners.get(player.getAdvancements());

		if (container != null) {
			container.trigger(predicate);
		}
	}
}
