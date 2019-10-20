package com.therandomlabs.randomportals.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.therandomlabs.randomportals.RandomPortals;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class PortalsTrigger extends RPOCriterionTrigger<AbstractCriterionInstance> {
	private static final ResourceLocation ID =
			new ResourceLocation(RandomPortals.MOD_ID, "portals");

	protected PortalsTrigger() {
		super(ID);
	}

	@Override
	public AbstractCriterionInstance deserializeInstance(
			JsonObject object,
			JsonDeserializationContext context
	) {
		return new AbstractCriterionInstance(ID);
	}

	public void trigger(EntityPlayerMP player) {
		trigger(player, instance -> true);
	}
}
