package com.therandomlabs.randomportals.advancements;

import java.util.Locale;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.api.frame.FrameType;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class ActivatedNetherPortalTrigger
		extends RPOCriterionTrigger<ActivatedNetherPortalTrigger.Instance> {
	public static final class Instance extends AbstractCriterionInstance {
		private final FrameType type;
		private final int size;

		public Instance(FrameType type, int size) {
			super(ID);
			this.type = type;
			this.size = size;
		}

		public boolean test(FrameType type, int size) {
			return this.type.test(type) && size >= this.size;
		}
	}

	private static final ResourceLocation ID =
			new ResourceLocation(RandomPortals.MOD_ID, "activated_nether_portal");

	protected ActivatedNetherPortalTrigger() {
		super(ID);
	}

	@Override
	public Instance deserializeInstance(JsonObject object, JsonDeserializationContext context) {
		return new Instance(
				FrameType.valueOf(object.get("type").getAsString().toUpperCase(Locale.ROOT)),
				object.get("size").getAsInt()
		);
	}

	public void trigger(EntityPlayerMP player, FrameType type, int size) {
		trigger(player, instance -> instance.test(type, size));
	}
}
