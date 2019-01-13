package com.therandomlabs.randomportals.api.config;

import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.frame.Frame;
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
	//If null, the sending portal frame type is used
	public FrameType generatedFrameType = FrameType.SAME;
	//If any of the sizes are 0, the sending portal size is used
	public FrameSizeData generatedFrameSize = new FrameSizeData();

	public DestinationData() {
		generatedFrameSize.lateral = new FrameSize(0, 0, 0, 0);
		generatedFrameSize.verticalX = new FrameSize(0, 0, 0, 0);
		generatedFrameSize.verticalZ = new FrameSize(0, 0, 0, 0);
	}

	public void ensureCorrect() {}

	public FrameSizeData getGeneratedFrameSize(@Nullable Frame frame) {
		if(frame == null) {
			return generatedFrameSize;
		}

		final int width = frame.getWidth();
		final int height = frame.getHeight();

		final FrameSizeData newGeneratedFrameSize = new FrameSizeData();

		newGeneratedFrameSize.lateral = getSize(generatedFrameSize.lateral, width, height);
		newGeneratedFrameSize.verticalX = getSize(generatedFrameSize.verticalX, width, height);
		newGeneratedFrameSize.verticalZ = getSize(generatedFrameSize.verticalZ, width, height);

		return newGeneratedFrameSize;
	}

	private FrameSize getSize(FrameSize size, int width, int height) {
		final FrameSize newSize = new FrameSize(
				size.minWidth, size.maxWidth, size.minHeight, size.maxHeight
		);

		if(size.minWidth == 0) {
			newSize.minWidth = width;
		}

		if(size.maxWidth == 0) {
			newSize.maxWidth = width;
		}

		if(size.minHeight == 0) {
			newSize.minHeight = height;
		}

		if(size.maxHeight == 0) {
			newSize.maxHeight = height;
		}

		newSize.ensureCorrect();
		return newSize;
	}
}
