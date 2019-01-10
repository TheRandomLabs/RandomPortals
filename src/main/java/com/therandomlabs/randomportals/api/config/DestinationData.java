package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.api.frame.FrameType;

public final class DestinationData {
	public int dimensionID = -1;

	public boolean teleportToPortal = true;

	public boolean generatePortalIfNotFound = true;
	public boolean generateUsingReceivingDimensionPortalType = true;

	public FrameType generatedFrameType = FrameType.VERTICAL;
	public FrameSizeData generatedFrameSize = new FrameSizeData();
	public boolean randomizeGeneratedFrameBlocks = true;

	public DestinationData() {
		generatedFrameSize.lateral = new FrameSize(4, 4, 5, 5);
		generatedFrameSize.verticalX = new FrameSize(4, 4, 5, 5);
		generatedFrameSize.verticalZ = new FrameSize(4, 4, 5, 5);
	}

	public void ensureCorrect() {}
}
