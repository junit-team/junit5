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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.params.support.FieldContext;
import org.junit.platform.commons.JUnitException;

class ContainerTemplateInstanceFieldInjector implements TestInstancePostProcessor {

	private final ParameterizedContainerClassContext classContext;
	private final EvaluatedArgumentSet arguments;
	private final int invocationIndex;

	ContainerTemplateInstanceFieldInjector(ParameterizedContainerClassContext classContext,
			EvaluatedArgumentSet arguments, int invocationIndex) {
		this.classContext = classContext;
		this.arguments = arguments;
		this.invocationIndex = invocationIndex;
	}

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
		this.classContext.getResolverFacade().getAllParameterDeclarations().stream() //
				.filter(FieldParameterDeclaration.class::isInstance) //
				.map(FieldContext.class::cast) //
				.forEach(fieldContext -> setField(fieldContext, testInstance, extensionContext));
	}

	private void setField(FieldContext fieldContext, Object testInstance, ExtensionContext extensionContext) {
		Object argument = this.classContext.getResolverFacade() //
				.resolve(fieldContext, extensionContext, this.arguments.getConsumedPayloads(), this.invocationIndex);
		try {
			fieldContext.getField().set(testInstance, argument);
		}
		catch (Exception e) {
			throw new JUnitException("Failed to inject parameter value into field: " + fieldContext.getField(), e);
		}
	}
}
