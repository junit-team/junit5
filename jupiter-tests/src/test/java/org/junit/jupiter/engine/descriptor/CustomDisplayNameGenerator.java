/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayNameGenerator;

public class CustomDisplayNameGenerator implements DisplayNameGenerator {

	@Override
	public String generateDisplayNameForClass(Class<?> testClass) {
		return "class-display-name";
	}

	@Override
	public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
		return "nested-class-display-name";
	}

	@Override
	public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
		return "method-display-name";
	}
}
