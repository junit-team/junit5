/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.ClassSelector.selectClass;
import static org.junit.platform.engine.discovery.UniqueIdSelector.selectUniqueId;
import static org.junit.platform.launcher.core.TestDiscoveryRequestBuilder.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.discovery.JUnit5UniqueIdBuilder;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.TestDiscoveryRequest;

/**
 * Test correct test discovery in simple test classes for the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class DiscoveryTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void discoverTestClass() {
		TestDiscoveryRequest request = request().selectors(selectClass(LocalTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(5, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void doNotDiscoverAbstractTestClass() {
		TestDiscoveryRequest request = request().selectors(selectClass(AbstractTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(0, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByUniqueId() {
		TestDiscoveryRequest request = request().selectors(
			selectUniqueId(JUnit5UniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test1()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByMethodReference() throws NoSuchMethodException {
		Method testMethod = LocalTestCase.class.getDeclaredMethod("test3", new Class[0]);

		TestDiscoveryRequest request = request().selectors(
			MethodSelector.selectMethod(LocalTestCase.class, testMethod)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverCompositeSpec() {
		TestDiscoveryRequest spec = request().selectors(
			selectUniqueId(JUnit5UniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test2()")),
			selectClass(LocalTestCase.class)).build();

		TestDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(5, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	// -------------------------------------------------------------------

	private static abstract class AbstractTestCase {
		@Test
		void abstractTest() {

		}
	}

	private static class LocalTestCase {

		@Test
		void test1() {

		}

		@Test
		void test2() {

		}

		@Test
		void test3() {

		}

		@CustomTestAnnotation
		void customTestAnnotation() {
			/* no-op */
		}

	}

	@Test
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}
}
