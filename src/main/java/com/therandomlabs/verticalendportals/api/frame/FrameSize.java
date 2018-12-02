package com.therandomlabs.verticalendportals.api.frame;

public class FrameSize {
	public int minWidth = 3;
	public int maxWidth = 9000;
	public int minHeight = 3;
	public int maxHeight = 9000;

	public FrameSize() {}

	public FrameSize(int minWidth, int maxWidth, int minHeight, int maxHeight) {
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
	}

	public boolean ensureCorrect() {
		boolean modified = false;

		if(minWidth < 3) {
			minWidth = 3;
			modified = true;
		}

		if(minHeight < 3) {
			minHeight = 3;
			modified = true;
		}

		if(maxWidth < minWidth) {
			maxWidth = minWidth;
			modified = true;
		}

		if(maxHeight < minHeight) {
			maxHeight = minHeight;
			modified = true;
		}

		return modified;
	}
}
