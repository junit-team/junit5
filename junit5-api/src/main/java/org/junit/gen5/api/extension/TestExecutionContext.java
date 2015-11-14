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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestExecutionContext {

	default Optional<Class<?>> getTestClass() {
		return Optional.empty();
	}

	default Optional<Object> getTestInstance() {
		return Optional.empty();
	}

	default Optional<Method> getTestMethod() {
		return Optional.empty();
	}

	default Optional<Throwable> getTestException() {
		return Optional.empty();
	}

	default Optional<TestExecutionContext> getParent() {
		return Optional.empty();
	}

	default Set<MethodArgumentResolver> getResolvers() {
		return Collections.emptySet();
	}

	String getDisplayName();

	Map<String, Object> getAttributes();

}
