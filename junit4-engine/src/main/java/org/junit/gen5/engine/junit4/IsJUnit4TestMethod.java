/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static org.junit.gen5.commons.util.ReflectionUtils.isPublic;
import static org.junit.gen5.commons.util.ReflectionUtils.isStatic;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.Test;

/**
 * @since 5.0
 */
public class IsJUnit4TestMethod implements Predicate<Method> {

	@Override
	public boolean test(Method candidate) {
		//Don't collapse into single return (JL)
		if (isStatic(candidate))
			return false;
		if (!isPublic(candidate))
			return false;
		//Don't use AnnotationUtils.hasAnnotation since JUnit4 does not support meta annotations
		return candidate.isAnnotationPresent(Test.class);
	}

}
