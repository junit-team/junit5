/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testers;

import static org.junit.gen5.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.gen5.commons.util.ReflectionUtils.isPrivate;
import static org.junit.gen5.commons.util.ReflectionUtils.isStatic;

import java.util.function.Predicate;

import org.junit.gen5.api.Nested;

/**
 * Test if an inner class is a JUnit 5 nested test class.
 *
 * @since 5.0
 */
public class IsNestedTestClass implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (isStatic(candidate))
			return false;
		if (isPrivate(candidate))
			return false;
		if (!candidate.isMemberClass())
			return false;
		return isAnnotated(candidate, Nested.class);
	}

}
