/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.dsl;

import java.lang.reflect.Method;

import org.junit.gen5.engine.MethodSpecification;

public class MethodTestPlanSpecificationElementBuilder {
	public static MethodSpecification forMethod(Class<?> testClass, Method testMethod) {
		return new MethodSpecification(testClass, testMethod);
	}

	public static MethodSpecification forMethod(MethodConfig methodConfig) {
		return new MethodSpecification(methodConfig.getTestClass(), methodConfig.getTestMethod());
	}

	public static MethodSpecification forMethod(String testClassName, String testMethodName) {
		return forMethod(new MethodConfig(testClassName, testMethodName));
	}

	public static MethodSpecification forMethod(Class<?> testClass, String testMethodName) {
		return forMethod(new MethodConfig(testClass.getName(), testMethodName));
	}
}
