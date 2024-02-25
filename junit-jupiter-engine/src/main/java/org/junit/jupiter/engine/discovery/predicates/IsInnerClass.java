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
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;

import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * Test if a class is a non-private inner class (i.e., a non-static nested class).
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class IsInnerClass implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> candidate) {
		// Do not collapse into a single return statement.
		if (isPrivate(candidate)) {
			return false;
		}
		if (!isInnerClass(candidate)) {
			return false;
		}

		return true;
	}

}
