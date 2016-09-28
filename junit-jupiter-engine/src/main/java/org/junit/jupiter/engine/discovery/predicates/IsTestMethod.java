/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.platform.commons.util.ReflectionUtils.isAbstract;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.isStatic;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.meta.API;

/**
 * Test if a method is a JUnit Jupiter test method.
 *
 * @since 5.0
 */
@API(Internal)
public class IsTestMethod implements Predicate<Method> {

	private static final IsPotentialTestMethod isPotentialTestMethod = new IsPotentialTestMethod();

	@Override
	public boolean test(Method candidate) {
		return isPotentialTestMethod.test(candidate) && isAnnotated(candidate, Test.class);
	}

}
