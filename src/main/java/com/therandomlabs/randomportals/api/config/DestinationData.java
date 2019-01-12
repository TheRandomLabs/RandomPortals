package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.api.frame.FrameType;

public final class DestinationData {
	public enum PortalGenerationBehavior {
		RANDOMIZE,
		CLONE,
		USE_RECEIVING_DIMENSION_PORTAL_TYPE
	}

	public int dimensionID = -1;

	public boolean teleportToPortal = true;
	public boolean oneWay;

	public boolean generatePortalIfNotFound = true;
	public PortalGenerationBehavior portalGenerationBehavior = PortalGenerationBehavior.CLONE;

	//Has no effect if portalGenerationBehavior is CLONE unless the receiving portal does not
	//have a valid frame
	public FrameType generatedFrameType = FrameType.VERTICAL;
	public FrameSizeData generatedFrameSize = new FrameSizeData();

	public DestinationData() {
		generatedFrameSize.lateral = new FrameSize(4, 4, 5, 5);
		generatedFrameSize.verticalX = new FrameSize(4, 4, 5, 5);
		generatedFrameSize.verticalZ = new FrameSize(4, 4, 5, 5);
	}

	public void ensureCorrect() {}
}
