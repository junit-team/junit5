/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension.sub;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.jupiter.api.extension.TestExtensionContext;

/**
 * Intentionally in a subpackage in order to properly test deactivation
 * of conditions based on patterns. In other words, we do not want this
 * condition declared in the same package as the
 * {@link org.junit.jupiter.engine.extension.DisabledCondition}
 *
 * @since 5.0
 */
public class SystemPropertyCondition implements TestExecutionCondition, ContainerExecutionCondition {

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@ExtendWith(SystemPropertyCondition.class)
	public @interface SystemProperty {

		String key();

		String value();
	}

	@Override
	public ConditionEvaluationResult evaluate(ContainerExtensionContext context) {
		return evaluate((ExtensionContext) context);
	}

	@Override
	public ConditionEvaluationResult evaluate(TestExtensionContext context) {
		return evaluate((ExtensionContext) context);
	}

	private ConditionEvaluationResult evaluate(ExtensionContext context) {
		Optional<SystemProperty> optional = findAnnotation(context.getElement(), SystemProperty.class);

		if (optional.isPresent()) {
			SystemProperty systemProperty = optional.get();
			String key = systemProperty.key();
			String expected = systemProperty.value();
			String actual = System.getProperty(key);

			if (!Objects.equals(expected, actual)) {
				return ConditionEvaluationResult.disabled(
					String.format("System property [%s] has a value of [%s] instead of [%s]", key, actual, expected));
			}
		}

		return ConditionEvaluationResult.enabled("@SystemProperty is not present");
	}

}
