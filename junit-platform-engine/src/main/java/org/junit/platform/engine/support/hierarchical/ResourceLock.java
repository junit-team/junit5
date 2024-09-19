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

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Optional;
import java.util.SortedSet;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * A lock for a one or more resources.
 *
 * @since 1.3
 * @see HierarchicalTestExecutorService.TestTask#getResourceLock()
 */
@API(status = STABLE, since = "1.10")
public interface ResourceLock extends AutoCloseable {

	/**
	 * Acquire this resource lock, potentially blocking.
	 *
	 * @return this lock so it can easily be used in a try-with-resources
	 * statement.
	 * @throws InterruptedException if the calling thread is interrupted
	 * while waiting to acquire this lock
	 */
	ResourceLock acquire() throws InterruptedException;

	/**
	 * Release this resource lock.
	 */
	void release();

	@Override
	default void close() {
		release();
	}

	SortedSet<ExclusiveResource> getResources();

	/**
	 * {@return whether the given lock is compatible with this lock}
	 * @param other the other lock to check for compatibility
	 */
	default boolean isCompatible(ResourceLock other) {

		SortedSet<ExclusiveResource> ownResources = this.getResources();
		SortedSet<ExclusiveResource> otherResources = other.getResources();

		if (ownResources.isEmpty() || otherResources.isEmpty()) {
			return true;
		}

		Preconditions.condition(ExclusiveResource.COMPARATOR.equals(ownResources.comparator()),
			() -> String.format("this.getResources() must be sorted according to %s, but were sorted according to %s",
				ExclusiveResource.COMPARATOR, ownResources.comparator()));
		Preconditions.condition(ExclusiveResource.COMPARATOR.equals(otherResources.comparator()),
			() -> String.format("other.getResources() must be sorted according to %s, but were sorted according to %s",
				ExclusiveResource.COMPARATOR, otherResources.comparator()));

		Optional<ExclusiveResource> potentiallyDeadlockCausingAdditionalResource = otherResources.stream() //
				.filter(resource -> !ownResources.contains(resource)) //
				.findFirst() //
				.filter(resource -> ExclusiveResource.COMPARATOR.compare(resource, ownResources.last()) < 0);

		return !(potentiallyDeadlockCausingAdditionalResource.isPresent());
	}
}
