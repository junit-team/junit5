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

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

class ParameterizedContainerFieldInjector implements TestInstancePostProcessor {

	private final ParameterizedContainerClassContext classContext;
	private final EvaluatedArgumentSet arguments;

	ParameterizedContainerFieldInjector(ParameterizedContainerClassContext classContext,
			EvaluatedArgumentSet arguments) {
		this.classContext = classContext;
		this.arguments = arguments;
	}

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
		Map<Field, Integer> parameterFields = classContext.getParameterFields();
		for (Map.Entry<Field, Integer> entry : parameterFields.entrySet()) {
			Field field = entry.getKey();
			int index = entry.getValue();
			// TODO #878 test preconditions
			Preconditions.condition(index >= 0 && index < arguments.getConsumedLength(),
				() -> String.format("Index declared on %s must be in range [0, %d]", field,
					arguments.getConsumedLength() - 1));
			try {
				field.set(testInstance, arguments.getConsumedPayloads()[index]);
			}
			catch (Exception e) {
				throw new JUnitException("Failed to inject parameter value into field: " + field, e);
			}
		}
	}
}
