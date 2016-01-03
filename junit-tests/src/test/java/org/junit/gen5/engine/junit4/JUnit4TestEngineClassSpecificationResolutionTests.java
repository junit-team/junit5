/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

class JUnit4TestEngineClassSpecificationResolutionTests {

	JUnit4TestEngine engine = new JUnit4TestEngine();

	@Test
	void resolvesSimpleJUnit4TestClass() {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		TestPlanSpecification specification = build(forClass(testClass));

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertTrue(runnerDescriptor.isContainer());
		assertFalse(runnerDescriptor.isTest());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName(), runnerDescriptor.getUniqueId());

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTrue(childDescriptor.isTest());
		assertFalse(childDescriptor.isContainer());
		// TODO #40 Get rid of "(className)" suffix?
		assertEquals("test(" + testClass.getName() + ")", childDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName() + "/test(" + testClass.getName() + ")",
			childDescriptor.getUniqueId());
		assertThat(childDescriptor.getChildren()).isEmpty();
	}

	@Test
	void resolvesIgnoredJUnit4TestClass() {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		TestPlanSpecification specification = build(forClass(testClass));

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertFalse(runnerDescriptor.isContainer());
		assertTrue(runnerDescriptor.isTest());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName(), runnerDescriptor.getUniqueId());
		assertThat(runnerDescriptor.getChildren()).isEmpty();
	}

	public static class PlainJUnit4TestCaseWithSingleTestWhichFails {

		@org.junit.Test
		public void test() {
			Assert.fail("this test should fail");
		}

	}

	@org.junit.Ignore
	public static class IgnoredJUnit4TestCase {

		@org.junit.Test
		public void test() {
			Assert.fail("this test should fail");
		}

	}

	private static <T> T getOnlyElement(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		Preconditions.condition(iterator.hasNext(), () -> "iterable must not be empty: " + iterable);
		T result = iterator.next();
		Preconditions.condition(!iterator.hasNext(), () -> "iterable must not contain more than one item: " + iterable);
		return result;
	}
}
