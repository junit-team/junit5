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

import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.api.*;
import org.junit.gen5.api.extension.*;

/**
 * {@link MethodParameterResolver} that injects a TestReporter.
 *
 * @since 5.0
 */
public class TestReporterParameterResolver implements MethodParameterResolver {

	@Override
	public boolean supports(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) {
		return (parameter.getType() == TestReporter.class);
	}

	@Override
	public TestReporter resolve(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) {
		return entry -> extensionContext.publishReportEntry(entry);
	}

}
