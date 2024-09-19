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

import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

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

	/**
	 * {@return the exclusive resources this lock represents}
	 */
	List<ExclusiveResource> getResources();

	/**
	 * {@return whether the given lock is compatible with this lock}
	 * @param other the other lock to check for compatibility
	 */
	default boolean isCompatible(ResourceLock other) {

		List<ExclusiveResource> ownResources = this.getResources();
		List<ExclusiveResource> otherResources = other.getResources();

		if (ownResources.isEmpty() || otherResources.isEmpty()) {
			return true;
		}

		Optional<ExclusiveResource> potentiallyDeadlockCausingAdditionalResource = otherResources.stream() //
				.filter(resource -> !ownResources.contains(resource)) //
				.findFirst() //
				.filter(resource -> ExclusiveResource.COMPARATOR.compare(resource,
					ownResources.get(ownResources.size() - 1)) < 0);

		return !(potentiallyDeadlockCausingAdditionalResource.isPresent());
	}
}
