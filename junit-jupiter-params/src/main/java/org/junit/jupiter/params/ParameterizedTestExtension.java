/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/**
 * @since 5.0
 */
class ParameterizedTestExtension extends ParameterizedInvocationContextProvider<TestTemplateInvocationContext>
		implements TestTemplateInvocationContextProvider {

	static final String DECLARATION_CONTEXT_KEY = "context";

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		Optional<ParameterizedTest> annotation = findAnnotation(context.getTestMethod(), ParameterizedTest.class);
		if (!annotation.isPresent()) {
			return false;
		}

		ParameterizedTestMethodContext methodContext = new ParameterizedTestMethodContext(
			context.getRequiredTestMethod(), annotation.get());

		getStore(context).put(DECLARATION_CONTEXT_KEY, methodContext);

		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
			ExtensionContext extensionContext) {

		return provideInvocationContexts(extensionContext, getDeclarationContext(extensionContext));
	}

	@Override
	public boolean mayReturnZeroTestTemplateInvocationContexts(ExtensionContext extensionContext) {
		return getDeclarationContext(extensionContext).isAllowingZeroInvocations();
	}

	private ParameterizedTestMethodContext getDeclarationContext(ExtensionContext extensionContext) {
		return getStore(extensionContext)//
				.get(DECLARATION_CONTEXT_KEY, ParameterizedTestMethodContext.class);
	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(ParameterizedTestExtension.class, context.getRequiredTestMethod()));
	}

}
