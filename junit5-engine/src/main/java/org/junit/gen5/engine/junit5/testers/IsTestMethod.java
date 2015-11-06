/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testers;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.gen5.api.Test;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public class IsTestMethod extends ReflectionObjectTester implements Predicate<Method> {

	@Override
	public boolean test(Method testMethodCandidate) {
		if (isPrivate(testMethodCandidate))
			return false;
		return hasAnnotation(testMethodCandidate, Test.class);
	}

}
