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

import java.lang.reflect.AnnotatedElement;

class ParameterizedContainerClassContext implements ParameterizedDeclarationContext<ParameterizedContainer> {

	private final Class<?> clazz;
	private final ParameterizedContainer annotation;
	private final ResolverFacade resolverFacade;

	ParameterizedContainerClassContext(Class<?> clazz, ParameterizedContainer annotation) {
		this.clazz = clazz;
		this.annotation = annotation;
		this.resolverFacade = ResolverFacade.create(clazz);
	}

	@Override
	public ParameterizedContainer getAnnotation() {
		return this.annotation;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return this.clazz;
	}

	@Override
	public String getDisplayNamePattern() {
		return this.annotation.name();
	}

	@Override
	public boolean isAllowingZeroInvocations() {
		// TODO #878 Read from annotation
		return false;
	}

	@Override
	public ArgumentCountValidationMode getArgumentCountValidationMode() {
		// TODO #878 Read from annotation
		return ArgumentCountValidationMode.DEFAULT;
	}

	@Override
	public ResolverFacade getResolverFacade() {
		return this.resolverFacade;
	}
}
