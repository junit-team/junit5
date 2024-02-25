/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.jupiter.api.Nested;

/**
 * Test if a class is a JUnit Jupiter {@link Nested @Nested} test class.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class IsNestedTestClass implements Predicate<Class<?>> {

	private static final IsInnerClass isInnerClass = new IsInnerClass();

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (!isInnerClass.test(candidate)) {
			return false;
		}
		return isAnnotated(candidate, Nested.class);
	}

}
