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

import java.util.function.Predicate;

/**
 * @since 5.0
 */
public class CanBeTestClass extends ReflectionObjectTester implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> testClassCandidate) {
		if (isAbstract(testClassCandidate))
			return false;
		if (testClassCandidate.isMemberClass() && !isStatic(testClassCandidate))
			return false;
		return (!testClassCandidate.isLocalClass());
	}

}
