package com.therandomlabs.verticalendportals.api.frame;

import com.therandomlabs.verticalendportals.api.util.StatePredicate;
import net.minecraft.init.Blocks;

public final class RequiredCorner {
	public static final StatePredicate ANY = (world, pos, state) -> true;
	public static final StatePredicate ANY_NON_AIR =
			(world, pos, state) -> state.getBlock() != Blocks.AIR;
	public static final StatePredicate SAME = null;

	private RequiredCorner() {}
}
