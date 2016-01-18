/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.Collections.unmodifiableSet;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

/**
 * Immutable data transfer object that represents a test or container which is
 * usually part of a {@link TestPlan}.
 *
 * @since 5.0
 * @see TestPlan
 */
public final class TestIdentifier implements Serializable {
	private static final long serialVersionUID = 1L;

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
		this.tags = unmodifiableSet(new LinkedHashSet<>(tags));
		this.test = test;
		this.container = container;
		this.parentId = parentId.orElse(null);
	}

	/**
	 * Get the unique ID of the represented test or container.
	 *
	 * <p>Uniqueness must be guaranteed across an entire
	 * {@linkplain TestPlan test plan}, regardless of how many engines are used
	 * behind the scenes.
	 */
	public TestId getUniqueId() {
		return uniqueId;
	}

	/**
	 * Get the display name for the represented test or container.
	 *
	 * <p>The <em>display name</em> is a human-readable name for a test or
	 * container. It must not be parsed or processed besides being displayed
	 * to end-users.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns {@code true} if this identifier represents a test.
	 */
	public boolean isTest() {
		return test;
	}

	/**
	 * Returns {@code true} if this identifier represents a container.
	 */
	public boolean isContainer() {
		return container;
	}

	/**
	 * Get the {@linkplain TestSource source location} of the represented test
	 * or container, if available.
	 *
	 * @see TestSource
	 */
	public Optional<TestSource> getSource() {
		return Optional.ofNullable(source);
	}

	/**
	 * Get the set of {@linkplain TestTag tags} of the represented test or
	 * container.
	 *
	 * @see TestTag
	 */
	public Set<TestTag> getTags() {
		return tags;
	}

	/**
	 * Get the unique ID of this identifier's parent, if available.
	 *
	 * <p>An identifier without a parent ID is called a <em>root</em>.
	 */
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

	@Override
	public String toString() {
		return getDisplayName() + " [" + uniqueId + "]";
	}
}
