package com.therandomlabs.randomportals.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public final class RPOCriteriaTriggers {
	public static final PortalsTrigger PORTALS = CriteriaTriggers.register(new PortalsTrigger());
	public static final ActivatedNetherPortalTrigger ACTIVATED_NETHER_PORTAL =
			CriteriaTriggers.register(new ActivatedNetherPortalTrigger());
	public static final DyedNetherPortalTrigger DYED_NETHER_PORTAL =
			CriteriaTriggers.register(new DyedNetherPortalTrigger());

	public static void register() {}
}
