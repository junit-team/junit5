/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

public final class TestIdentifier {

	private final TestId uniqueId;
	private final String displayName;
	private final TestSource source;
	private final Set<TestTag> tags;

	private final boolean test;
	private final boolean container;

	private final TestId parentId;

	public static TestIdentifier from(TestDescriptor testDescriptor) {
		// TODO Use Flyweight Pattern for TestId?
		TestId uniqueId = new TestId(testDescriptor.getUniqueId());
		String displayName = testDescriptor.getDisplayName();
		Optional<TestSource> source = testDescriptor.getSource();
		Set<TestTag> tags = testDescriptor.getTags();
		boolean test = testDescriptor.isTest();
		boolean container = !test || !testDescriptor.getChildren().isEmpty();
		Optional<TestId> parentId = testDescriptor.getParent().map(TestDescriptor::getUniqueId).map(TestId::new);
		return new TestIdentifier(uniqueId, displayName, source, tags, test, container, parentId);
	}

	TestIdentifier(TestId uniqueId, String displayName, Optional<TestSource> source, Set<TestTag> tags, boolean test,
			boolean container, Optional<TestId> parentId) {
		this.uniqueId = uniqueId;
		this.displayName = displayName;
		this.source = source.orElse(null);
		this.tags = new LinkedHashSet<>(tags);
		this.test = test;
		this.container = container;
		this.parentId = parentId.orElse(null);
	}

	/**
	 * Get the unique identifier (UID) for the described test.
	 *
	 * <p>Uniqueness must be guaranteed across an entire test plan,
	 * regardless of how many engines are used behind the scenes.
	 */
	public TestId getUniqueId() {
		return uniqueId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isTest() {
		return test;
	}

	public boolean isContainer() {
		return container;
	}

	public Optional<TestSource> getSource() {
		return Optional.ofNullable(source);
	}

	public Set<TestTag> getTags() {
		return tags;
	}

	public Optional<TestId> getParentId() {
		return Optional.ofNullable(parentId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestIdentifier) {
			TestIdentifier that = (TestIdentifier) obj;
			return Objects.equals(this.uniqueId, that.uniqueId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return uniqueId.hashCode();
	}

}
