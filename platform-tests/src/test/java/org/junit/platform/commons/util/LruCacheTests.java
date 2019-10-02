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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.HashMap;
import java.util.stream.IntStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @since 1.6
 */
class LruCacheTests {

	@ParameterizedTest
	@MethodSource("smallInts")
	void evictsEldestEntryWhenMaxSizeIsReached(int maxSize) throws Exception {
		var cache = new LruCache<Integer, Integer>(maxSize);

		cache.put(0, 0);
		var initialCapacity = getCapacity(cache);
		assertThat(initialCapacity).isEqualTo(nextPowerOfTwo(maxSize));

		IntStream.rangeClosed(1, maxSize).forEach(i -> cache.put(i, i));

		assertThat(getCapacity(cache)).isEqualTo(initialCapacity);
		assertThat(cache) //
				.doesNotContain(entry(0, 0)) //
				.hasSize(maxSize);
	}

	private int nextPowerOfTwo(int n) {
		return Integer.highestOneBit(n) * 2;
	}

	private static IntStream smallInts() {
		return IntStream.rangeClosed(1, 64);
	}

	private int getCapacity(HashMap<?, ?> cache) throws Exception {
		var field = HashMap.class.getDeclaredField("table");
		var value = ReflectionUtils.tryToReadFieldValue(field, cache).get();
		return ((Object[]) value).length;
	}

}
