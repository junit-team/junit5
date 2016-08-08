/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DiscoverySelectors}.
 *
 * @since 1.0
 */
public class DiscoverySelectorsTests {

	private static final Method fullyQualifiedMethod = fullyQualifiedMethod();
	private static final Method fullyQualifiedMethodWithParameters = fullyQualifiedMethodWithParameters();

	private static final String fullyQualifiedMethodName = fullyQualifiedMethodName();
	private static final String fullyQualifiedMethodNameWithParameters = fullyQualifiedMethodNameWithParameters();

	@Test
	void selectPackageByName() {
		PackageSelector selector = DiscoverySelectors.selectPackage(getClass().getPackage().getName());
		assertEquals(getClass().getPackage().getName(), selector.getPackageName());
	}

	@Test
	void selectClassByName() {
		ClassSelector selector = DiscoverySelectors.selectClass(getClass().getName());
		assertEquals(getClass(), selector.getJavaClass());
	}

	@Test
	void selectMethodByFullyQualifiedName() {
		MethodSelector selector = selectMethod(fullyQualifiedMethodName);
		assertEquals(fullyQualifiedMethod, selector.getJavaMethod());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithParameters() {
		MethodSelector selector = selectMethod(fullyQualifiedMethodNameWithParameters);
		assertEquals(fullyQualifiedMethodWithParameters, selector.getJavaMethod());
	}

	@Test
	void selectMethodWithParametersByMethodReference() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		MethodSelector selector = selectMethod(getClass(), method);
		assertEquals(method, selector.getJavaMethod());
		assertEquals(method, selector.getJavaMethod());
	}

	private static String fullyQualifiedMethodName() {
		return String.format("%s#%s()", DiscoverySelectorsTests.class.getName(), fullyQualifiedMethod().getName());
	}

	private static String fullyQualifiedMethodNameWithParameters() {
		return String.format("%s#%s(%s)", DiscoverySelectorsTests.class.getName(), fullyQualifiedMethod().getName(),
			String.class.getName());
	}

	private static Method fullyQualifiedMethod() {
		try {
			return DiscoverySelectorsTests.class.getDeclaredMethod("myTest");
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static Method fullyQualifiedMethodWithParameters() {
		try {
			return DiscoverySelectorsTests.class.getDeclaredMethod("myTest", String.class);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	void myTest() {
	}

	void myTest(String info) {
	}

}
