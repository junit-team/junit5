/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;

import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * Test if a class is a JUnit Jupiter test class which should be included in
 * package and classpath scanning.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class IsScannableTestClass implements Predicate<Class<?>> {

	private static final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (isPrivate(candidate)) {
			return false;
		}
		return isTestClassWithTests.test(candidate);
	}

}
