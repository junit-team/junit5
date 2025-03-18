
/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.platform.commons.support.ReflectionSupport;

public class IntrospectiveDisplayNameGenerator implements DisplayNameGenerator {

	@Override
	public String generateDisplayNameForClass(Class<?> testClass) {
		return getDelegate(testClass).generateDisplayNameForClass(testClass);
	}

	@Override
	public String generateDisplayNameForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> nestedClass) {
		return getDelegate(nestedClass).generateDisplayNameForNestedClass(enclosingInstanceTypes, nestedClass);
	}

	@Override
	public String generateDisplayNameForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass,
			Method testMethod) {
		return getDelegate(testClass).generateDisplayNameForMethod(enclosingInstanceTypes, testClass, testMethod);
	}

	private static DisplayNameGenerator getDelegate(Class<?> testClass) {
		if (DisplayNameGenerator.class.isAssignableFrom(testClass)) {
			return (DisplayNameGenerator) ReflectionSupport.newInstance(testClass);
		}
		return DisplayNameGenerator.getDisplayNameGenerator(Standard.class);
	}
}
