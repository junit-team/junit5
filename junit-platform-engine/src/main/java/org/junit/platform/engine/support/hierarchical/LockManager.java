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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.platform.commons.annotation.UseResource;

class LockManager {

	private final Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

	CompositeLock getLocks(List<UseResource> resources) {
		return new CompositeLock(resources.stream().sorted(comparing(UseResource::value)).map(resource -> {
			String key = resource.value();
			ReentrantReadWriteLock lock = locks.computeIfAbsent(key, k -> new ReentrantReadWriteLock());
			if (resource.mode() == UseResource.LockMode.Read) {
				return lock.readLock();
			}
			else {
				return lock.writeLock();
			}
		}).collect(toList()));
	}

}
