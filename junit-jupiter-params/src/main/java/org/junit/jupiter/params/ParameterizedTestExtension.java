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

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class ParameterizedTestExtension extends ParameterizedInvocationContextProvider<TestTemplateInvocationContext>
		implements TestTemplateInvocationContextProvider {

	static final String DECLARATION_CONTEXT_KEY = "context";

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		if (!context.getTestMethod().isPresent()) {
			return false;
		}

		Method templateMethod = context.getRequiredTestMethod();
		Optional<ParameterizedTest> annotation = findAnnotation(templateMethod, ParameterizedTest.class);
		if (!annotation.isPresent()) {
			return false;
		}

		ParameterizedTestMethodContext methodContext = new ParameterizedTestMethodContext(templateMethod,
			annotation.get());

		Preconditions.condition(methodContext.hasPotentiallyValidSignature(),
			() -> String.format(
				"@ParameterizedTest method [%s] declares formal parameters in an invalid order: "
						+ "argument aggregators must be declared after any indexed arguments "
						+ "and before any arguments resolved by another ParameterResolver.",
				templateMethod.toGenericString()));

		getStore(context).put(DECLARATION_CONTEXT_KEY, methodContext);

		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
			ExtensionContext extensionContext) {

		ParameterizedTestMethodContext declarationContext = getDeclarationContext(extensionContext);

		return provideInvocationContexts(extensionContext, declarationContext, //
			(formatter, arguments, invocationIndex) -> createInvocationContext(formatter, declarationContext, arguments,
				invocationIndex));
	}

	@Override
	public boolean mayReturnZeroTestTemplateInvocationContexts(ExtensionContext extensionContext) {
		ParameterizedTestMethodContext methodContext = getDeclarationContext(extensionContext);
		return methodContext.annotation.allowZeroInvocations();
	}

	private ParameterizedTestMethodContext getDeclarationContext(ExtensionContext extensionContext) {
		return getStore(extensionContext)//
				.get(DECLARATION_CONTEXT_KEY, ParameterizedTestMethodContext.class);
	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(ParameterizedTestExtension.class, context.getRequiredTestMethod()));
	}

	private TestTemplateInvocationContext createInvocationContext(ParameterizedInvocationNameFormatter formatter,
			ParameterizedTestMethodContext methodContext, Arguments arguments, int invocationIndex) {

		return new ParameterizedTestInvocationContext(formatter, methodContext, arguments, invocationIndex);
	}

}
