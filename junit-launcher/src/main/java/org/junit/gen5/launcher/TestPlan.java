package org.junit.gen5.launcher;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

// TODO make immutable?
public class TestPlan {

	private final List<TestIdentifier> testIdentifiers = new LinkedList<>();

	public List<TestIdentifier> getTestIdentifiers() {
		return testIdentifiers;
	}

	public void addTestIdentifiers(Collection<TestIdentifier> testDescriptions) {
		this.testIdentifiers.addAll(testDescriptions);
	}

}
