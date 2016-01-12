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

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder.forClass;
import static org.junit.gen5.engine.dsl.MethodTestPlanSpecificationElementBuilder.forMethod;
import static org.junit.gen5.engine.dsl.PackageTestPlanSpecificationElementBuilder.forPackage;

import java.lang.reflect.Method;
import java.util.*;

import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestPlanSpecificationElement;

public class NamedTestPlanSpecificationElementBuilder {
	public static TestPlanSpecificationElement forName(String anyName) {
		Optional<Class<?>> testClassOptional = ReflectionUtils.loadClass(anyName);
		if (testClassOptional.isPresent()) {
			return forClass(testClassOptional.get());
		}

		Optional<Method> testMethodOptional = ReflectionUtils.loadMethod(anyName);
		if (testMethodOptional.isPresent()) {
			Method testMethod = testMethodOptional.get();
			return forMethod(testMethod.getDeclaringClass(), testMethod);
		}

		if (ReflectionUtils.isPackage(anyName)) {
			return forPackage(anyName);
		}

		throw new PreconditionViolationException(
			String.format("'%s' specifies neither a class, a method, nor a package.", anyName));
	}

	public static List<TestPlanSpecificationElement> forNames(String... classNames) {
		if (classNames != null) {
			return forNames(Arrays.asList(classNames));
		}
		else {
			return Collections.emptyList();
		}
	}

	public static List<TestPlanSpecificationElement> forNames(Collection<String> classNames) {
		return classNames.stream().map(NamedTestPlanSpecificationElementBuilder::forName).collect(toList());
	}
}
