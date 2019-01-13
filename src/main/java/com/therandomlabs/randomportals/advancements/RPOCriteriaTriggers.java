package com.therandomlabs.randomportals.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public final class RPOCriteriaTriggers {
	public static final ActivatedNetherPortalTrigger ACTIVATED_NETHER_PORTAL =
			CriteriaTriggers.register(new ActivatedNetherPortalTrigger());

	public static void register() {}
}
