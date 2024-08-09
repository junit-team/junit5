/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Unit tests for {@link IsTestTemplateMethod}.
 *
 * @since 5.0
 */
class IsTestTemplateMethodTests {

	private static final IsTestTemplateMethod isTestTemplateMethod = new IsTestTemplateMethod();

	@Test
	void testTemplateMethodReturningVoid() {
		assertThat(isTestTemplateMethod).accepts(method("templateReturningVoid"));
	}

	@Test
	void bogusTestTemplateMethodReturningObject() {
		assertThat(isTestTemplateMethod).rejects(method("bogusTemplateReturningObject"));
	}

	private static Method method(String name) {
		return ReflectionUtils.findMethod(ClassWithTestTemplateMethods.class, name).get();
	}

	private static class ClassWithTestTemplateMethods {

		@TestTemplate
		void templateReturningVoid() {
		}

		@TestTemplate
		String bogusTemplateReturningObject() {
			return "";
		}

	}

}
