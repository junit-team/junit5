/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;

import org.junit.jupiter.engine.descriptor.MethodTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestMethod;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.0
 */
@API(Experimental)
class TestMethodResolver extends AbstractMethodResolver {

	static final String SEGMENT_TYPE = "method";

	TestMethodResolver() {
		super(SEGMENT_TYPE, new IsTestMethod());
	}

	protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method) {
		return new MethodTestDescriptor(uniqueId, testClass, method);
	}

}
