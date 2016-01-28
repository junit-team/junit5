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
		Method method = ReflectionUtils.findMethod(Sample.class, "methodWithTestReporterParameter",
			new Class[] { TestReporter.class }).get();
		Parameter parameter = method.getParameters()[0];

		assertTrue(resolver.supports(parameter, null, null));

		//	assertTrue(resolver.supports(null, null, null));

	}

	@Test
	void testResolve() {

	}

}

class Sample {

	public void methodWithTestReporterParameter(TestReporter reporter) {

	}

}
