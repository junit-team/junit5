/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
 * TODO
 */
// TODO: @API(...)
public interface ExclusiveResourceProvider extends Extension {
	default Set<ExclusiveResource> provideExclusiveResourcesForClass(Class<?> testClass,
			Set<ExclusiveResource> declaredResources) {
		// return declaredResources;
		return Collections.emptySet();
	}

	default Set<ExclusiveResource> provideExclusiveResourcesForNestedClass(Class<?> nestedClass,
			Set<ExclusiveResource> declaredResources) {
		// return declaredResources;
		return Collections.emptySet();
	}

	default Set<ExclusiveResource> provideExclusiveResourcesForMethod(Class<?> testClass, Method testMethod,
			Set<ExclusiveResource> declaredResources) {
		// return declaredResources;
		return Collections.emptySet();
	}
}
