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

import static java.util.Collections.emptyNavigableSet;
import static java.util.Collections.unmodifiableNavigableSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.singleton;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @since 1.3
 */
class LockManager {

	private final Map<String, ReadWriteLock> locksByKey = new ConcurrentHashMap<>();
	private final SingleLock globalReadLock;
	private final SingleLock globalReadWriteLock;

	public LockManager() {
		globalReadLock = new SingleLock(GLOBAL_READ, toLock(GLOBAL_READ));
		globalReadWriteLock = new SingleLock(GLOBAL_READ_WRITE, toLock(GLOBAL_READ_WRITE));
	}

	ResourceLock getLockForResources(Collection<ExclusiveResource> resources) {
		return toResourceLock(toDistinctSortedResources(resources));
	}

	ResourceLock getLockForResource(ExclusiveResource resource) {
		return toResourceLock(singleton(resource));
	}

	private NavigableSet<ExclusiveResource> toDistinctSortedResources(Collection<ExclusiveResource> resources) {
		if (resources.isEmpty()) {
			return emptyNavigableSet();
		}
		if (resources.size() == 1) {
			return singleton(getOnlyElement(resources));
		}
		// @formatter:off
		Map<String, List<ExclusiveResource>> resourcesByKey = resources.stream()
				.sorted(ExclusiveResource.COMPARATOR)
				.distinct()
				.collect(groupingBy(ExclusiveResource::getKey, LinkedHashMap::new, toList()));

		NavigableSet<ExclusiveResource> result = resourcesByKey.values().stream()
				.map(resourcesWithSameKey -> resourcesWithSameKey.get(0))
				.collect(toCollection(() -> new TreeSet<>(ExclusiveResource.COMPARATOR)));
		// @formatter:on

		return unmodifiableNavigableSet(result);
	}

	private ResourceLock toResourceLock(NavigableSet<ExclusiveResource> resources) {
		switch (resources.size()) {
			case 0:
				return NopLock.INSTANCE;
			case 1:
				ExclusiveResource resource = getOnlyElement(resources);
				if (GLOBAL_READ.equals(resource)) {
					return globalReadLock;
				}
				if (GLOBAL_READ_WRITE.equals(resource)) {
					return globalReadWriteLock;
				}
				return new SingleLock(resources, toLock(resource));
			default:
				return new CompositeLock(resources, toLocks(resources));
		}
	}

	private List<Lock> toLocks(Set<ExclusiveResource> resources) {
		return resources.stream().map(this::toLock).collect(toUnmodifiableList());
	}

	private Lock toLock(ExclusiveResource resource) {
		ReadWriteLock lock = this.locksByKey.computeIfAbsent(resource.getKey(), key -> new ReentrantReadWriteLock());
		return resource.getLockMode() == READ ? lock.readLock() : lock.writeLock();
	}

}
