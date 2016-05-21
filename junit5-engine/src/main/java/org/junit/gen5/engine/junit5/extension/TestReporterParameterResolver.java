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

import java.lang.reflect.Parameter;

import org.junit.gen5.api.TestReporter;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.commons.util.Preconditions;

/**
 * {@link MethodParameterResolver} that injects a {@link TestReporter}.
 *
 * @since 5.0
 */
class TestReporterParameterResolver implements MethodParameterResolver {

	@Override
	public boolean supports(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) {
		Preconditions.notNull(parameter, "supplied parameter must not be null");

		return (parameter.getType() == TestReporter.class);
	}

	@Override
	public TestReporter resolve(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) {
		return extensionContext::publishReportEntry;
	}

}
