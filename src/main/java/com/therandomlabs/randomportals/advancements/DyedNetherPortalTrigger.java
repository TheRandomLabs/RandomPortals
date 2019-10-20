package com.therandomlabs.randomportals.advancements;

import java.util.Locale;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.therandomlabs.randomportals.RandomPortals;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

public class DyedNetherPortalTrigger extends RPOCriterionTrigger<DyedNetherPortalTrigger.Instance> {
	public static final class Instance extends AbstractCriterionInstance {
		private final EnumDyeColor color;
		private final boolean rightClickSinglePortalBlock;

		public Instance(EnumDyeColor color, boolean rightClickSinglePortalBlock) {
			super(ID);
			this.color = color;
			this.rightClickSinglePortalBlock = rightClickSinglePortalBlock;
		}

		public boolean test(EnumDyeColor color, boolean rightClickSinglePortalBlock) {
			return this.color == color &&
					this.rightClickSinglePortalBlock == rightClickSinglePortalBlock;
		}
	}

	private static final ResourceLocation ID =
			new ResourceLocation(RandomPortals.MOD_ID, "dyed_nether_portal");

	protected DyedNetherPortalTrigger() {
		super(ID);
	}

	@Override
	public Instance deserializeInstance(JsonObject object, JsonDeserializationContext context) {
		return new Instance(
				EnumDyeColor.valueOf(object.get("color").getAsString().toUpperCase(Locale.ROOT)),
				object.get("rightClickSinglePortalBlock").getAsBoolean()
		);
	}

	public void trigger(
			EntityPlayerMP player, EnumDyeColor color,
			boolean rightClickSinglePortalBlock
	) {
		trigger(player, instance -> instance.test(color, rightClickSinglePortalBlock));
	}
}
