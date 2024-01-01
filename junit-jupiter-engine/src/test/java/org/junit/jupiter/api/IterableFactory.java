/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class IterableFactory {

	static List<Object> listOf(Object... objects) {
		return Arrays.asList(objects);
	}

	static Set<Object> setOf(Object... objects) {
		return new LinkedHashSet<>(listOf(objects));
	}

}
