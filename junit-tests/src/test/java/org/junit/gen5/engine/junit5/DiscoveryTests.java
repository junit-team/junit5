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
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Test correct test discovery in simple test classes for the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class DiscoveryTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void discoverTestClass() {
		TestPlanSpecification spec = build(forClass(LocalTestCase.class));
		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverByUniqueId() {
		TestPlanSpecification spec = build(
			forUniqueId("junit5:org.junit.gen5.engine.junit5.DiscoveryTests$LocalTestCase#test1()"));
		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(2, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverByMethod() throws NoSuchMethodException {
		Method testMethod = LocalTestCase.class.getDeclaredMethod("test3", new Class[0]);

		TestPlanSpecification spec = build(forMethod(LocalTestCase.class, testMethod));
		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(2, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverCompositeSpec() {
		TestPlanSpecification spec = build(
			forUniqueId("junit5:org.junit.gen5.engine.junit5.DiscoveryTests$LocalTestCase#test2()"),
			forClass(LocalTestCase.class));

		EngineDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	// -------------------------------------------------------------------

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
