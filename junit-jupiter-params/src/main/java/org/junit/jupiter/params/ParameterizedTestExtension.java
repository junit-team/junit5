/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static java.util.Collections.emptyIterator;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.util.Iterator;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

class ParameterizedTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supports(ContainerExtensionContext context) {
		// @formatter:off
		return context.getTestMethod()
				.filter(method -> isAnnotated(method, ParameterizedTest.class))
				.map(method -> true)
				.orElse(false);
		// @formatter:on
	}

	@Override
	public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
		// TODO
		return emptyIterator();
	}
}
