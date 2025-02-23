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

import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;

class ParameterizedContainerClassContext
		implements ParameterizedDeclarationContext<ContainerTemplateInvocationContext> {

	private final Class<?> clazz;
	private final ParameterizedContainer annotation;
	private final ResolverFacade resolverFacade;

	ParameterizedContainerClassContext(Class<?> clazz, ParameterizedContainer annotation) {
		this.clazz = clazz;
		this.annotation = annotation;
		this.resolverFacade = ResolverFacade.create(clazz, annotation);
	}

	@Override
	public ParameterizedContainer getAnnotation() {
		return this.annotation;
	}

	@Override
	public Class<?> getAnnotatedElement() {
		return this.clazz;
	}

	@Override
	public String getDisplayNamePattern() {
		return this.annotation.name();
	}

	@Override
	public boolean isAutoClosingArguments() {
		return this.annotation.autoCloseArguments();
	}

	@Override
	public boolean isAllowingZeroInvocations() {
		return this.annotation.allowZeroInvocations();
	}

	@Override
	public ArgumentCountValidationMode getArgumentCountValidationMode() {
		return this.annotation.argumentCountValidation();
	}

	@Override
	public ResolverFacade getResolverFacade() {
		return this.resolverFacade;
	}

	@Override
	public ContainerTemplateInvocationContext createInvocationContext(ParameterizedInvocationNameFormatter formatter,
			Arguments arguments, int invocationIndex) {
		return new ParameterizedContainerInvocationContext(this, formatter, arguments, invocationIndex);
	}
}
