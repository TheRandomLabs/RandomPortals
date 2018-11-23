package com.therandomlabs.verticalendportals;

public class CommonProxy {
	public void construct() {}

	public void preInit() {
		VEPConfig.reload();
	}

	public void init() {}
}
