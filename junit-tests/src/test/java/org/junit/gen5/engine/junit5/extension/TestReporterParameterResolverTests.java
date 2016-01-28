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
import org.junit.gen5.api.extension.*;
import org.junit.gen5.commons.util.*;
import org.mockito.*;

class TestReporterParameterResolverTests {

	TestReporterParameterResolver resolver = new TestReporterParameterResolver();

	@Test
	void testSupports() {
		Parameter parameter1 = findParameterOfMethod("methodWithTestReporterParameter",
			new Class[] { TestReporter.class });
		assertTrue(this.resolver.supports(parameter1, null, null));

		Parameter parameter2 = findParameterOfMethod("methodWithoutTestReporterParameter",
			new Class[] { String.class });
		assertFalse(this.resolver.supports(parameter2, null, null));
	}

	@Test
	void testSupports_NullSafe() {
		assertThrows(PreconditionViolationException.class, () -> {
			this.resolver.supports(null, null, null);
		});
	}

	@Test
	void testResolve() {
		Parameter parameter = findParameterOfMethod("methodWithTestReporterParameter",
			new Class[] { TestReporter.class });

		TestReporter testReporter = this.resolver.resolve(parameter, null, Mockito.mock(ExtensionContext.class));
		assertNotNull(testReporter);
	}

	private Parameter findParameterOfMethod(String methodName, Class[] parameterTypes) {
		Method method = ReflectionUtils.findMethod(Sample.class, methodName, parameterTypes).get();
		return method.getParameters()[0];
	}

	static class Sample {

		public void methodWithTestReporterParameter(TestReporter reporter) {
		}

		public void methodWithoutTestReporterParameter(String nothing) {
		}

	}

}
