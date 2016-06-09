/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery.predicates;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.function.Predicate;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * Test if a class is a JUnit 5 test class which should be included in package and classpath scanning.
 *
 * @since 5.0
 */
@API(Internal)
public class IsScannableTestClass implements Predicate<Class<?>> {

	private static final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (ReflectionUtils.isPrivate(candidate))
			return false;
		return isTestClassWithTests.test(candidate);
	}

}
