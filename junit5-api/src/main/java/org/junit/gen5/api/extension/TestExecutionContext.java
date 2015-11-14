/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestExecutionContext {

	Optional<Class<?>> getTestClass();

	Optional<Object> getTestInstance();

	Optional<Method> getTestMethod();

	Optional<TestExecutionContext> getParent();

	default Optional<Throwable> getTestException() {
		return Optional.empty();
	}

	Set<MethodArgumentResolver> getResolvers();

	String getDisplayName();

	Map<String, Object> getAttributes();

}
