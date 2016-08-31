/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.vintage.rulesupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.rules.ExternalResource;

public class ExternalResourceSupport implements BeforeEachCallback, AfterEachCallback {

	private final Class<Rule> annotationType = Rule.class;
	private final Class<ExternalResource> ruleType = ExternalResource.class;

	@Override
	public void beforeEach(TestExtensionContext context) throws Exception {
		// TODO: generalize to methods returning rule instances!
		this.invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::before);
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		// TODO: generalize to methods returning rule instances!
		this.invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::after);
	}

	private void invokeAppropriateMethodOnRuleAnnotatedMembers(TestExtensionContext context,
			Consumer<GenericBeforeAndAfterAdvice> methodCaller) {
		// @formatter:off
        Stream<AbstractTestRuleAdapter> ruleAdapters = this.findRuleAnnotatedFields(context)
                .map(field -> new RuleAnnotatedField(context, field))
                .map(annotatedField -> new ExternalResourceAdapter(annotatedField.getTestRuleInstance()));
		// @formatter:on

		ruleAdapters.forEach(methodCaller::accept);
	}

	private Stream<Field> findRuleAnnotatedFields(TestExtensionContext context) {
		Object testInstance = context.getTestInstance();
		return findAnnotatedFields(testInstance, this.ruleType, this.annotationType);
	}

	// TODO: decide whether this should be promoted to AnnotationUtils
	private static Stream<Field> findAnnotatedFields(Object instance, Class<?> fieldType,
			Class<? extends Annotation> annotationType) {
		Field[] declaredFields = instance.getClass().getDeclaredFields();

		// @formatter:off
        return Arrays.stream(declaredFields)
                .filter(field -> fieldType.isAssignableFrom(field.getType()))
                .filter(field -> field.isAnnotationPresent(annotationType));
		// @formatter:on
	}

}
