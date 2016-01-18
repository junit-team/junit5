/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.descriptor;

import static java.util.function.Predicate.isEqual;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.commons.util.FunctionUtils.where;
import static org.junit.gen5.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.engine.support.descriptor.JavaSource;
import org.junit.runner.Description;

class JavaSourceExtractor {

	static Optional<JavaSource> toJavaSource(Description description) {
		Class<?> testClass = description.getTestClass();
		if (testClass != null) {
			String methodName = description.getMethodName();
			if (methodName != null) {
				return Optional.of(toJavaMethodSource(testClass, methodName));
			}
			return Optional.of(new JavaSource(testClass));
		}
		return Optional.empty();
	}

	private static JavaSource toJavaMethodSource(Class<?> testClass, String methodName) {
		List<Method> methods = findMethods(testClass, where(Method::getName, isEqual(methodName)));
		if (methods.size() == 1) {
			return new JavaSource(getOnlyElement(methods));
		}
		return new JavaSource(testClass);
	}

}
