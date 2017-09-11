/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

public class ModuleTestFinder implements ModuleClassFinder {

	@Test
	void test() {
	}

	@Override
	public List<Class<?>> findAllClassesInModule(String moduleName, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		return Collections.singletonList(getClass());
	}
}
