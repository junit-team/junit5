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

import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * @since 5.13
 */
class ParameterizedContainerExtension extends ParameterizedInvocationContextProvider<ContainerTemplateInvocationContext>
		implements ContainerTemplateInvocationContextProvider {

	static final String DECLARATION_CONTEXT_KEY = "context";

	@Override
	public boolean supportsContainerTemplate(ExtensionContext context) {
		if (!context.getTestClass().isPresent()) {
			return false;
		}

		Class<?> testClass = context.getRequiredTestClass();
		Optional<ParameterizedContainer> annotation = findAnnotation(testClass, ParameterizedContainer.class);
		if (!annotation.isPresent()) {
			return false;
		}

		ParameterizedContainerClassContext classContext = new ParameterizedContainerClassContext(testClass,
			annotation.get());

		// TODO #878 Validate signature of test class constructor
		//		Preconditions.condition(classContext.hasPotentiallyValidSignature(),
		//				() -> String.format(
		//						"@ParameterizedTest method [%s] declares formal parameters in an invalid order: "
		//								+ "argument aggregators must be declared after any indexed arguments "
		//								+ "and before any arguments resolved by another ParameterResolver.",
		//						templateMethod.toGenericString()));

		getStore(context).put(DECLARATION_CONTEXT_KEY, classContext);

		return true;
	}

	@Override
	public Stream<? extends ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
			ExtensionContext extensionContext) {

		return provideInvocationContexts(extensionContext, getDeclarationContext(extensionContext));
	}

	@Override
	public boolean mayReturnZeroContainerTemplateInvocationContexts(ExtensionContext extensionContext) {
		return getDeclarationContext(extensionContext).isAllowingZeroInvocations();
	}

	private ParameterizedContainerClassContext getDeclarationContext(ExtensionContext extensionContext) {
		return getStore(extensionContext)//
				.get(DECLARATION_CONTEXT_KEY, ParameterizedContainerClassContext.class);
	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(ParameterizedTestExtension.class, context.getRequiredTestClass()));
	}

}
