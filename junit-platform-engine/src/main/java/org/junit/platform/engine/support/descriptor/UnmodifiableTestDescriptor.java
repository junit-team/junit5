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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.meta.API.Usage;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * Decorator for an {@link TestDescriptor} that throws {@link UnsupportedOperationException}
 * for any mutable operations.
 *
 * <p>Methods that are not supported are annotated with {@code @Deprecated}.
 *
 * @since 1.0
 */
@API(Usage.Experimental)
public final class UnmodifiableTestDescriptor implements TestDescriptor {
	private final TestDescriptor delegate;

	public UnmodifiableTestDescriptor(TestDescriptor testDescriptor) {
		this.delegate = Preconditions.notNull(testDescriptor, "testDescriptor must not be null");
	}

	@Override
	public UniqueId getUniqueId() {
		return delegate.getUniqueId();
	}

	@Override
	public String getDisplayName() {
		return delegate.getDisplayName();
	}

	@Override
	public Set<TestTag> getTags() {
		return Collections.unmodifiableSet(delegate.getTags());
	}

	@Override
	public Optional<TestSource> getSource() {
		return delegate.getSource();
	}

	@Override
	public Optional<TestDescriptor> getParent() {
		return delegate.getParent().map(UnmodifiableTestDescriptor::new);
	}

	@Override
	@Deprecated
	public void setParent(TestDescriptor parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<? extends TestDescriptor> getChildren() {
		// @formatter:off
		Set<? extends TestDescriptor> wrapped = delegate.getChildren().stream()
				.map(UnmodifiableTestDescriptor::new)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		// @formatter:on
		return Collections.unmodifiableSet(wrapped);
	}

	@Override
	@Deprecated
	public void addChild(TestDescriptor descriptor) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void removeChild(TestDescriptor descriptor) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void removeFromHierarchy() {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void prune() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRoot() {
		return delegate.isRoot();
	}

	@Override
	public Type getType() {
		return delegate.getType();
	}

	@Override
	public Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId) {
		return delegate.findByUniqueId(uniqueId).map(UnmodifiableTestDescriptor::new);
	}

	@Override
	public void accept(Visitor visitor) {
		delegate.accept(d -> visitor.visit(new UnmodifiableTestDescriptor(d)));
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof UnmodifiableTestDescriptor)) {
			return false;
		}
		UnmodifiableTestDescriptor that = (UnmodifiableTestDescriptor) other;
		return this.delegate.equals(that.delegate);
	}
}
