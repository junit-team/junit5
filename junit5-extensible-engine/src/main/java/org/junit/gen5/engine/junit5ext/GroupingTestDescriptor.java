package org.junit.gen5.engine.junit5ext;

import org.junit.gen5.engine.AbstractTestDescriptor;

public class GroupingTestDescriptor extends AbstractTestDescriptor {
	private String displayName;

	public GroupingTestDescriptor(String uniqueId, String displayName) {
		super(uniqueId);
		this.displayName = displayName;
	}

	@Override public String getDisplayName() {
		return displayName;
	}

	@Override public boolean isTest() {
		return false;
	}
}
