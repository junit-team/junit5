/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static java.util.Collections.emptySet;
import static org.junit.platform.commons.meta.API.Status.STABLE;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * Abstract base implementation of {@link TestDescriptor} that may be used by
 * custom {@link org.junit.platform.engine.TestEngine TestEngines}.
 *
 * <p>Subclasses should provide a {@code source} in their constructor, if
 * possible, and override {@link #getTags}, if appropriate.
 *
 * @since 1.0
 */
@API(status = STABLE)
public abstract class AbstractTestDescriptor implements TestDescriptor {

	private final UniqueId uniqueId;

	private final String displayName;

	private TestDescriptor parent;

	private final TestSource source;

	private final Set<TestDescriptor> children = Collections.synchronizedSet(new LinkedHashSet<>(16));

	/**
	 * Create a new {@code AbstractTestDescriptor} with the supplied
	 * {@link UniqueId} and display name.
	 *
	 * @param uniqueId the unique ID of this {@code TestDescriptor}; never
	 * {@code null}
	 * @param displayName the display name for this {@code TestDescriptor};
	 * never {@code null} or blank
	 * @see #AbstractTestDescriptor(UniqueId, String, TestSource)
	 */
	protected AbstractTestDescriptor(UniqueId uniqueId, String displayName) {
		this(uniqueId, displayName, null);
	}

	/**
	 * Create a new {@code AbstractTestDescriptor} with the supplied
	 * {@link UniqueId}, display name, and source.
	 *
	 * @param uniqueId the unique ID of this {@code TestDescriptor}; never
	 * {@code null}
	 * @param displayName the display name for this {@code TestDescriptor};
	 * never {@code null} or blank
	 * @param source the source of the test or container described by this
	 * {@code TestDescriptor}; can be {@code null}
	 * @see #AbstractTestDescriptor(UniqueId, String)
	 */
	protected AbstractTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
		this.uniqueId = Preconditions.notNull(uniqueId, "UniqueId must not be null");
		this.displayName = Preconditions.notBlank(displayName, "displayName must not be null or blank");
		this.source = source;
	}

	@Override
	public final UniqueId getUniqueId() {
		return this.uniqueId;
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	@Override
	public Optional<TestDescriptor> getParent() {
		return Optional.ofNullable(this.parent);
	}

	@Override
	public final void setParent(TestDescriptor parent) {
		this.parent = parent;
	}

	@Override
	public void removeChild(TestDescriptor child) {
		Preconditions.notNull(child, "child must not be null");
		this.children.remove(child);
		child.setParent(null);
	}

	@Override
	public void removeFromHierarchy() {
		if (isRoot()) {
			throw new JUnitException("You cannot remove the root of a hierarchy.");
		}
		this.parent.removeChild(this);
		this.children.clear();
	}

	@Override
	public Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId) {
		Preconditions.notNull(uniqueId, "UniqueId must not be null");
		if (getUniqueId().equals(uniqueId)) {
			return Optional.of(this);
		}
		// @formatter:off
		return this.children.stream()
				.map(child -> child.findByUniqueId(uniqueId))
				.filter(Optional::isPresent)
				.findAny()
				.orElse(Optional.empty());
		// @formatter:on
	}

	@Override
	public void addChild(TestDescriptor child) {
		Preconditions.notNull(child, "child must not be null");
		child.setParent(this);
		this.children.add(child);
	}

	@Override
	public final Set<? extends TestDescriptor> getChildren() {
		return Collections.unmodifiableSet(this.children);
	}

	@Override
	public Set<TestTag> getTags() {
		return emptySet();
	}

	@Override
	public Optional<TestSource> getSource() {
		return Optional.ofNullable(this.source);
	}

	@Override
	public final int hashCode() {
		return this.uniqueId.hashCode();
	}

	@Override
	public final boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this.getClass() != other.getClass()) {
			return false;
		}
		TestDescriptor that = (TestDescriptor) other;
		return this.getUniqueId().equals(that.getUniqueId());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getUniqueId();
	}

}
