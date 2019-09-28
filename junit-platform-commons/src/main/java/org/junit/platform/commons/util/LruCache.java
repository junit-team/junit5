/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apiguardian.api.API;

/**
 * A simple LRU cache with a maximum size.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of values maintained by this cache
 */
@API(status = INTERNAL, since = "1.6")
public class LruCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	private int maxSize;

	public LruCache(int maxSize) {
		super(maxSize + 1, 0.75f, true);
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}

}
