/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectName;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * Unit tests for {@link DefaultDiscoveryRequest}.
 *
 * @since 1.0
 */
public class DiscoveryRequestTests {

	private static final Method fullyQualifiedMethod = fullyQualifiedMethod();

	private static final String fullyQualifiedMethodName = fullyQualifiedMethodName();

	@Test
	void selectMethodByFullyQualifiedName() throws Exception {
		MethodSelector selector = selectMethod(fullyQualifiedMethodName);
		assertEquals(fullyQualifiedMethod, selector.getJavaMethod());
	}

	@Test
	void selectNameWithPackageName() {
		DiscoverySelector selector = selectName("org.junit.platform");
		assertEquals(PackageSelector.class, selector.getClass());
	}

	@Test
	void selectNameWithClassName() {
		DiscoverySelector selector = selectName(getClass().getName());
		assertEquals(ClassSelector.class, selector.getClass());
	}

	@Test
	void selectNameWithMethodName() throws Exception {
		DiscoverySelector selector = selectName(fullyQualifiedMethodName);
		assertEquals(MethodSelector.class, selector.getClass());
	}

	@Test
	void buildDiscoveryRequest() {
		// @formatter:off
		EngineDiscoveryRequest spec = request().selectors(
			selectUniqueId(UniqueId.forEngine("fooEngine")),
			selectPackage("org.junit.platform"),
			selectClass(getClass()),
			selectMethod(fullyQualifiedMethodName)
		).build();

		assertAll(
			() -> assertEquals(1, spec.getSelectorsByType(UniqueIdSelector.class).size()),
			() -> assertEquals(1, spec.getSelectorsByType(PackageSelector.class).size()),
			() -> assertEquals(1, spec.getSelectorsByType(ClassSelector.class).size()),
			() -> assertEquals(1, spec.getSelectorsByType(MethodSelector.class).size())
		);
		// @formatter:on
	}

	private static String fullyQualifiedMethodName() {
		return DiscoveryRequestTests.class.getName() + "#" + fullyQualifiedMethod().getName();
	}

	private static Method fullyQualifiedMethod() {
		try {
			return DiscoveryRequestTests.class.getDeclaredMethod("myTest");
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	void myTest() {
	}

}
