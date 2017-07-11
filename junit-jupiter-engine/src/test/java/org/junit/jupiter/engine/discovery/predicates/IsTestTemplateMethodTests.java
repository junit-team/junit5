/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.platform.commons.util.ReflectionUtils;

class IsTestTemplateMethodTests {

	@Test
	void testTemplateMethodReturningVoidEvaluatesToTrue() throws NoSuchMethodException {
		Method templateMethod = ReflectionUtils.findMethod(AClassWithTestTemplate.class, "templateReturningVoid").get();
		assertThat(templateMethod).matches(new IsTestTemplateMethod());
	}

	@Test
	void testTemplateMethodReturningObjectEvaluatesToFalse() throws NoSuchMethodException {
		Method templateMethod = ReflectionUtils.findMethod(AClassWithTestTemplate.class,
			"templateReturningObject").get();
		assertThat(templateMethod).matches(new IsTestTemplateMethod().negate(),
			"negated test template method predicate");
	}

	private static class AClassWithTestTemplate {

		@TestTemplate
		void templateReturningVoid() {
		}

		@TestTemplate
		String templateReturningObject() {
			return "";
		}

	}
}
