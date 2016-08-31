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

import static java.util.stream.Collectors.*;

import java.lang.reflect.*;
import java.util.*;

import org.junit.Rule;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.rules.ExternalResource;

public class ExternalResourceSupport implements BeforeEachCallback, AfterEachCallback {

	final Class<Rule> annotationType = Rule.class;
	final Class<ExternalResource> ruleType = ExternalResource.class;

	@Override
	public void beforeEach(TestExtensionContext context) throws Exception {
		// TODO: generalize to methods returning rule instances!
		this.invokeNamedMethodOnRuleAnnotatedFields(context, "before");
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		// TODO: generalize to methods returning rule instances!
		this.invokeNamedMethodOnRuleAnnotatedFields(context, "after");
	}

	private void invokeNamedMethodOnRuleAnnotatedFields(TestExtensionContext context, String name) {
		Object testInstance = context.getTestInstance();
		List<Field> externalResourceFields = this.findRuleAnnotatedFieldsOfTargetType(testInstance);

		externalResourceFields.forEach(field -> this.invokeNamedMethodSafely(name, testInstance, field));
	}

	private void invokeNamedMethodSafely(String name, Object testInstance, Field field) {
		try {
			invokeNamedMethod(testInstance, name, field);
		}
		catch (IllegalAccessException | NoSuchMethodException e) {
			//TODO: decide whether this should be logged
			e.printStackTrace();
		}
	}

	private void invokeNamedMethod(Object testInstance, String name, Field field)
			throws IllegalAccessException, NoSuchMethodException {
		ExternalResource externalResource = (ExternalResource) field.get(testInstance);

		Method method = externalResource.getClass().getDeclaredMethod(name);
		method.setAccessible(true);
		ReflectionUtils.invokeMethod(method, externalResource);
	}

	//TODO: decide whether this should be promoted to ReflectionUtils
	private List<Field> findRuleAnnotatedFieldsOfTargetType(Object testInstance) {
		Field[] declaredFields = testInstance.getClass().getDeclaredFields();

		// @formatter:off
        return Arrays.asList(declaredFields).stream()
                .filter(field -> this.ruleType.isAssignableFrom(field.getType()))
                .filter(field -> field.isAnnotationPresent(this.annotationType))
                .collect(toList());
		// @formatter:on
	}

}
