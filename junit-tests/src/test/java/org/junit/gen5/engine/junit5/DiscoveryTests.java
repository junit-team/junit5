/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.junit5.discovery.JUnit5UniqueIdBuilder;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Test correct test discovery in simple test classes for the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class DiscoveryTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void discoverTestClass() {
		TestDiscoveryRequest request = request().select(forClass(LocalTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void doNotDiscoverAbstractTestClass() {
		TestDiscoveryRequest request = request().select(forClass(AbstractTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(0, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByUniqueId() {
		TestDiscoveryRequest request = request().select(
			forUniqueId(JUnit5UniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test1()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByMethodReference() throws NoSuchMethodException {
		Method testMethod = LocalTestCase.class.getDeclaredMethod("test3", new Class[0]);

		TestDiscoveryRequest request = request().select(
			MethodSelector.forMethod(LocalTestCase.class, testMethod)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverCompositeSpec() {
		TestDiscoveryRequest spec = request().select(
			forUniqueId(JUnit5UniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test2()")),
			forClass(LocalTestCase.class)).build();

		TestDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
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
