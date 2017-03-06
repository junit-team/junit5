/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@link ParameterResolver} that injects a {@link TestReporter}.
 *
 * @since 5.0
 */
class TestReporterParameterResolver implements ParameterResolver {

	@Override
	public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == TestReporter.class);
	}

	@Override
	public TestReporter resolve(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return extensionContext::publishReportEntry;
	}

}
