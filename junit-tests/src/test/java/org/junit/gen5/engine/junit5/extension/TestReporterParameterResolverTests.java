/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.junit.gen5.api.Assertions.*;

import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.api.*;
import org.junit.gen5.commons.util.*;

class TestReporterParameterResolverTests {

	TestReporterParameterResolver resolver = new TestReporterParameterResolver();

	@Test
	void testSupports() {
		Method methodWithTestReporterParameter = ReflectionUtils.findMethod(Sample.class,
			"methodWithTestReporterParameter", new Class[] { TestReporter.class }).get();
		Parameter parameter1 = methodWithTestReporterParameter.getParameters()[0];
		assertTrue(resolver.supports(parameter1, null, null));

		Method methodWithoutTestReporterParameter = ReflectionUtils.findMethod(Sample.class,
			"methodWithoutTestReporterParameter", new Class[] { String.class }).get();
		Parameter parameter2 = methodWithoutTestReporterParameter.getParameters()[0];
		assertFalse(resolver.supports(parameter2, null, null));
	}

	@Test
	void testSupports_NullSafe() {
		assertThrows(PreconditionViolationException.class, () -> {
			resolver.supports(null, null, null);
		});
	}

	@Test
	void testResolve() {

	}

}

class Sample {

	public void methodWithTestReporterParameter(TestReporter reporter) {
	}

	public void methodWithoutTestReporterParameter(String nothing) {
	}

}
