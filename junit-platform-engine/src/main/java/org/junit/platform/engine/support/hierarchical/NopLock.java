/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.Collections.unmodifiableNavigableSet;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.platform.commons.util.ToStringBuilder;

/**
 * No-op {@link ResourceLock} implementation.
 *
 * @since 1.3
 */
class NopLock implements ResourceLock {

	static final ResourceLock INSTANCE = new NopLock();
	static final SortedSet<ExclusiveResource> RESOURCES = unmodifiableNavigableSet(
		new TreeSet<>(ExclusiveResource.COMPARATOR));

	private NopLock() {
	}

	@Override
	public SortedSet<ExclusiveResource> getResources() {
		return RESOURCES;
	}

	@Override
	public ResourceLock acquire() {
		return this;
	}

	@Override
	public void release() {
		// nothing to do
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).toString();
	}
}
