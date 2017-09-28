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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ModuleTestFinder implements ModuleClassFinder {

	@Override
	public List<Class<?>> findAllClassesInModule(ClassFilter filter, String moduleName) {
		return Collections.singletonList(getClass());
	}

	@Override
	public List<Class<?>> findAllClassesOnModulePath(ClassFilter filter) {
		return Collections.singletonList(getClass());
	}

	@Override
	public List<Class<?>> findAllClassesOnModulePath(ClassFilter filter, ClassLoader loader, Path... entries) {
		return Collections.singletonList(getClass());
	}
}
