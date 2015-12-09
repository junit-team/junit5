
package org.junit.gen5.engine.junit5ext.testable;

import java.util.*;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

public class TestGroup implements MutableTestDescriptor {
	private String displayName;
	private String uniqueId;
	private MutableTestDescriptor parent;

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
	public Optional<MutableTestDescriptor> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public void setParent(MutableTestDescriptor parent) {
		this.parent = parent;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TestGroup testGroup = (TestGroup) o;
		return Objects.equals(uniqueId, testGroup.uniqueId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uniqueId);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("TestGroup");
		sb.append("{displayName='").append(displayName).append('\'');
		sb.append(", uniqueId='").append(uniqueId).append('\'');
		sb.append(", parent=").append(parent);
		sb.append('}');
		return sb.toString();
	}
}
