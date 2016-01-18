/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.discovery;

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

/**
 * @since 5.0
 */
public abstract class AbstractTestDescriptor implements TestDescriptor {

	private final String uniqueId;

	private TestDescriptor parent;

	private TestSource source;

	private final Set<TestDescriptor> children = new LinkedHashSet<>();

	protected AbstractTestDescriptor(String uniqueId) {
		this.uniqueId = Preconditions.notBlank(uniqueId, "uniqueId must not be null or empty");
	}

	@Override
	public final String getUniqueId() {
		return this.uniqueId;
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
		this.children.remove(child);
		child.setParent(null);
	}

	protected void removeFromHierarchy() {
		if (isRoot()) {
			throw new JUnitException("You cannot remove the root of a hierarchy.");
		}
		this.parent.removeChild(this);
		this.children.clear();
	}

	@Override
	public Optional<? extends TestDescriptor> findByUniqueId(String uniqueId) {
		if (getUniqueId().equals(uniqueId)) {
			return Optional.of(this);
		}
		for (TestDescriptor child : this.children) {
			Optional<? extends TestDescriptor> result = child.findByUniqueId(uniqueId);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}

	@Override
	public void addChild(TestDescriptor child) {
		Preconditions.notNull(child, "child must not be null");
		child.setParent(this);
		this.children.add(child);
	}

	@Override
	public Set<? extends TestDescriptor> getChildren() {
		return Collections.unmodifiableSet(this.children);
	}

	protected final void setSource(TestSource source) {
		this.source = Preconditions.notNull(source, "TestSource must not be null");
	}

	@Override
	public void accept(Visitor visitor) {
		Runnable remove = this::removeFromHierarchy;
		visitor.visit(this, remove);
		new HashSet<>(getChildren()).forEach(child -> child.accept(visitor));
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
