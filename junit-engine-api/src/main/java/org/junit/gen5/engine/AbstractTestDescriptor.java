/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public abstract class AbstractTestDescriptor implements TestDescriptor {

	private final String uniqueId;

	private TestDescriptor parent;

	private TestSource source;

	private final Set<AbstractTestDescriptor> children = new LinkedHashSet<>();

	protected AbstractTestDescriptor(String uniqueId) {
		Preconditions.notBlank(uniqueId, "uniqueId must not be null or empty");
		this.uniqueId = uniqueId;
	}

	@Override
	public final String getUniqueId() {
		return this.uniqueId;
	}

	@Override
	public final TestDescriptor getParent() {
		return this.parent;
	}

	protected final void setParent(TestDescriptor parent) {
		Preconditions.notNull(parent, "parent must not be null");
		this.parent = parent;
	}

	public final void addChild(AbstractTestDescriptor child) {
		Preconditions.notNull(child, "child must not be null");
		child.setParent(this);
		this.children.add(child);
	}

	protected final void setSource(TestSource source) {
		Preconditions.notNull(source, "test source must not be null");
		this.source = source;
	}

	public final Set<AbstractTestDescriptor> getChildren() {
		return this.children;
	}

	@Override
	public Set<TestTag> getTags() {
		return Collections.emptySet();
	}

	@Override
	public Optional<TestSource> getSource() {
		return Optional.ofNullable(source);
	}

	@Override
	public final boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this.getClass() != other.getClass()) {
			return false;
		}
		TestDescriptor otherDescriptor = (TestDescriptor) other;
		return this.getUniqueId().equals(otherDescriptor.getUniqueId());
	}

	@Override
	public final int hashCode() {
		return this.uniqueId.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getUniqueId();
	}
}
