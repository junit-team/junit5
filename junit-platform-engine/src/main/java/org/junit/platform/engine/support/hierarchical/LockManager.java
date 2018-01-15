/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.annotation.LockMode.Read;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class LockManager {

	private final Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

	ResourceLock getLockForResources(List<ExclusiveResource> resources) {
		// @formatter:off
		List<Lock> locks = resources.stream()
				.distinct()
				.sorted()
				.map(resource -> {
					ReentrantReadWriteLock lock = this.locks.computeIfAbsent(resource.getKey(),
							key -> new ReentrantReadWriteLock());
					return resource.getLockMode() == Read ? lock.readLock() : lock.writeLock();
				})
				.collect(toList());
		// @formatter:on
		int size = locks.size();
		if (size == 0) {
			return NopLock.INSTANCE;
		}
		if (size == 1) {
			return new SingleLock(locks.get(0));
		}
		return new CompositeLock(locks);
	}

}
