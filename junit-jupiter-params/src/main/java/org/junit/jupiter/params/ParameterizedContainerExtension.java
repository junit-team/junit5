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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.JUnitException;

class ParameterizedContainerExtension extends ParameterizedInvocationContextProvider<ContainerTemplateInvocationContext>
		implements ContainerTemplateInvocationContextProvider {

	@Override
	public boolean supportsContainerTemplate(ExtensionContext extensionContext) {
		if (!extensionContext.getTestClass().isPresent()) {
			return false;
		}

		Class<?> testClass = extensionContext.getRequiredTestClass();
		Optional<ParameterizedContainer> annotation = findAnnotation(testClass, ParameterizedContainer.class);
		return annotation.isPresent();
	}

	@Override
	public Stream<? extends ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
			ExtensionContext extensionContext) {

		ParameterizedContainerClassContext declarationContext = getDeclarationContext(extensionContext);

		return provideInvocationContexts(extensionContext, declarationContext, //
			(formatter, arguments, invocationIndex) -> createInvocationContext(formatter, declarationContext, arguments,
				invocationIndex));
	}

	@Override
	public boolean mayReturnZeroContainerTemplateInvocationContexts(ExtensionContext context) {
		return getDeclarationContext(context).isAllowingZeroInvocations();
	}

	private static ParameterizedContainerClassContext getDeclarationContext(ExtensionContext extensionContext) {
		// TODO #878 Cache in Store

		Class<?> testClass = extensionContext.getRequiredTestClass();
		ParameterizedContainer annotation = findAnnotation(testClass, ParameterizedContainer.class).orElseThrow(
			() -> new JUnitException("No @ParameterizedContainer annotation found"));
		return new ParameterizedContainerClassContext(testClass, annotation);
	}

	private ContainerTemplateInvocationContext createInvocationContext(ParameterizedInvocationNameFormatter formatter,
			ParameterizedContainerClassContext classContext, Arguments arguments, int invocationIndex) {
		return new ParameterizedContainerInvocationContext(formatter, classContext, arguments, invocationIndex);
	}
}
