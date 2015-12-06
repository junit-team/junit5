
package org.junit.gen5.engine.junit5ext;

import java.util.*;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

public class TestGroup implements MutableTestDescriptor {
	private String displayName;
	private String uniqueId;

	public TestGroup(String uniqueId, String displayName) {
		this.uniqueId = uniqueId;
		this.displayName = displayName;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public Set<MutableTestDescriptor> getChildren() {
		return Collections.emptySet();
	}

	@Override
	public void addChild(MutableTestDescriptor descriptor) {
		throw new UnsupportedOperationException("TestGroup does not support addChild(), yet.");
	}

	@Override
	public void removeChild(MutableTestDescriptor descriptor) {
		throw new UnsupportedOperationException("TestGroup does not support removeChild(), yet.");
	}

	@Override
	public Optional<MutableTestDescriptor> getParent() {
		throw new UnsupportedOperationException("TestGroup does not support getParent(), yet.");
	}

	@Override
	public void setParent(MutableTestDescriptor parent) {
		throw new UnsupportedOperationException("TestGroup does not support setParent(), yet.");
	}

	@Override
	public Set<TestTag> getTags() {
		throw new UnsupportedOperationException("TestGroup does not support getTags(), yet.");
	}

	@Override
	public void accept(Visitor visitor) {
		throw new UnsupportedOperationException("TestGroup will not accept visitors.");
	}

	@Override
	public Optional<TestSource> getSource() {
		throw new UnsupportedOperationException("TestGroup will not support getSource().");
	}
}
