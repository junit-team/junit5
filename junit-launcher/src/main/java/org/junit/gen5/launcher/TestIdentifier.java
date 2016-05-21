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
import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ToStringBuilder;
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
@API(Experimental)
public final class TestIdentifier implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String uniqueId;
	private final String parentId;
	private final String name;
	private final String displayName;
	private final TestSource source;
	private final Set<TestTag> tags;
	private final boolean test;
	private final boolean container;

	/**
	 * Factory for creating a new {@link TestIdentifier} from a {@link TestDescriptor}.
	 */
	public static TestIdentifier from(TestDescriptor testDescriptor) {
		// TODO Use Flyweight Pattern for TestIdentifier?
		String uniqueId = testDescriptor.getUniqueId().toString();
		String name = testDescriptor.getName();
		String displayName = testDescriptor.getDisplayName();
		Optional<TestSource> source = testDescriptor.getSource();
		Set<TestTag> tags = testDescriptor.getTags();
		boolean test = testDescriptor.isTest();
		boolean container = !test || !testDescriptor.getChildren().isEmpty();
		Optional<String> parentId = testDescriptor.getParent().map(
			parentDescriptor -> parentDescriptor.getUniqueId().toString());
		return new TestIdentifier(uniqueId, name, displayName, source, tags, test, container, parentId);
	}

	private TestIdentifier(String uniqueId, String name, String displayName, Optional<TestSource> source,
			Set<TestTag> tags, boolean test, boolean container, Optional<String> parentId) {
		this.uniqueId = uniqueId;
		this.parentId = parentId.orElse(null);
		this.name = name;
		this.displayName = displayName;
		this.source = source.orElse(null);
		this.tags = unmodifiableSet(new LinkedHashSet<>(tags));
		this.test = test;
		this.container = container;
	}

	/**
	 * Get the unique ID of the represented test or container.
	 *
	 * <p>Uniqueness must be guaranteed across an entire
	 * {@linkplain TestPlan test plan}, regardless of how many engines are used
	 * behind the scenes.
	 */
	public String getUniqueId() {
		return this.uniqueId;
	}

	/**
	 * Get the unique ID of this identifier's parent, if available.
	 *
	 * <p>An identifier without a parent ID is called a <em>root</em>.
	 */
	public Optional<String> getParentId() {
		return Optional.ofNullable(this.parentId);
	}

	/**
	 * Get the name of the represented test or container.
	 *
	 * <p>The <em>name</em> is a technical name for a test or container. It
	 * must not be parsed or processed besides being displayed to end-users.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the display name of the represented test or container.
	 *
	 * <p>The <em>display name</em> is a human-readable name for a test or
	 * container. It must not be parsed or processed besides being displayed
	 * to end-users.
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Determine if this identifier represents a test.
	 */
	public boolean isTest() {
		return this.test;
	}

	/**
	 * Determine if this identifier represents a container.
	 */
	public boolean isContainer() {
		return this.container;
	}

	/**
	 * Get the {@linkplain TestSource source} of the represented test
	 * or container, if available.
	 *
	 * @see TestSource
	 */
	public Optional<TestSource> getSource() {
		return Optional.ofNullable(this.source);
	}

	/**
	 * Get the set of {@linkplain TestTag tags} associated with the represented
	 * test or container.
	 *
	 * @see TestTag
	 */
	public Set<TestTag> getTags() {
		return this.tags;
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
		return this.uniqueId.hashCode();
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("uniqueId", this.uniqueId)
				.append("parentId", this.parentId)
				.append("name", this.name)
				.append("displayName", this.displayName)
				.append("source", this.source)
				.append("tags", this.tags)
				.append("test", this.test)
				.append("container", this.container)
				.toString();
		// @formatter:on
	}

}
