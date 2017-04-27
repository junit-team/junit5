/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

/**
 * Unit tests for extension composability in JUnit Jupiter.
 *
 * @since 5.0
 * @see KitchenSinkExtension
 */
class ExtensionComposabilityTests {

	@Test
	void ensureJupiterExtensionApisAreComposable() {

		// 1) Find all Extension APIs
		List<Class<?>> extensionApis = ReflectionUtils.findAllClassesInPackage(Extension.class.getPackage().getName(),
			this::isExtensionApi, name -> true);

		// 2) Dynamically implement all Extension APIs
		Object dynamicKitchenSinkExtension = Proxy.newProxyInstance(getClass().getClassLoader(),
			extensionApis.toArray(new Class<?>[extensionApis.size()]), (proxy, method, args) -> null);

		// @formatter:off
		List<Method> expectedMethods = extensionApis.stream()
				.map(api -> api.getDeclaredMethods())
				.flatMap(Arrays::stream)
				.collect(toList());

		List<String> expectedMethodSignatures = expectedMethods.stream()
				.map(this::methodSignature)
				.sorted()
				.collect(toList());

		List<String> expectedMethodNames = expectedMethods.stream()
				.map(Method::getName)
				.sorted()
				.collect(toList());

		List<Method> actualMethods = Arrays.stream(dynamicKitchenSinkExtension.getClass().getDeclaredMethods())
				.collect(toList());

		List<String> actualMethodSignatures = actualMethods.stream()
				.map(this::methodSignature)
				.sorted()
				.collect(toList());

		List<String> actualMethodNames = actualMethods.stream()
				.map(Method::getName)
				.sorted()
				.collect(toList());
		// @formatter:on

		actualMethodSignatures.remove("equals(Object)");
		actualMethodSignatures.remove("hashCode()");
		actualMethodSignatures.remove("toString()");

		actualMethodNames.remove("equals");
		actualMethodNames.remove("hashCode");
		actualMethodNames.remove("toString");

		assertThat(actualMethodNames).isEqualTo(expectedMethodNames);
		assertThat(actualMethodSignatures).isEqualTo(expectedMethodSignatures);
	}

	public boolean isExtensionApi(Class<?> candidate) {
		return candidate.isInterface() && (candidate != Extension.class) && Extension.class.isAssignableFrom(candidate);
	}

	private String methodSignature(Method method) {
		return String.format("%s(%s)", method.getName(),
			StringUtils.nullSafeToString(Class::getSimpleName, method.getParameterTypes()));
	}

}
