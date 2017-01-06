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

public class IsTestTemplateMethodTests {

	@Test
	void publicTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method templateMethod = ReflectionUtils.findMethod(AClassWithTestTemplate.class, "template").get();
		assertThat(templateMethod).matches(new IsTestTemplateMethod());
	}

	private static class AClassWithTestTemplate {
		@TestTemplate
		void template() {
		}
	}
}
