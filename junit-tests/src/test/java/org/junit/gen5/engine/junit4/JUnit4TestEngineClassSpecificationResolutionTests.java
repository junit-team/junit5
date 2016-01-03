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

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit4.samples.IgnoredJUnit4TestCase;
import org.junit.gen5.engine.junit4.samples.JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.PlainJUnit3TestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.PlainJUnit4TestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.PlainOldJavaClassWithoutAnyTest;
import org.junit.gen5.engine.junit4.samples.SingleFailingTheoryTestCase;
import org.junit.gen5.engine.junit4.samples.TestCaseRunWithJUnit5;

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
		assertEquals("test", childDescriptor.getDisplayName());
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

	@Test
	void resolvesJUnit4TestClassWithCustomRunner() {
		Class<?> testClass = SingleFailingTheoryTestCase.class;
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
		assertEquals("theory", childDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName() + "/theory(" + testClass.getName() + ")",
			childDescriptor.getUniqueId());
		assertThat(childDescriptor.getChildren()).isEmpty();
	}

	@Test
	void resolvesJUnit3TestCase() {
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
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
		assertEquals("test", childDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName() + "/test(" + testClass.getName() + ")",
			childDescriptor.getUniqueId());
		assertThat(childDescriptor.getChildren()).isEmpty();
	}

	@Test
	void resolvesJUnit3Suites() {
		Class<?> suiteClass = JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails.class;
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
		TestPlanSpecification specification = build(forClass(suiteClass));

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertTrue(suiteDescriptor.isContainer());
		assertFalse(suiteDescriptor.isTest());
		assertThat(suiteDescriptor.getDisplayName()).startsWith("TestSuite with 1 tests");
		assertEquals("junit4:" + suiteClass.getName(), suiteDescriptor.getUniqueId());

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertTrue(testClassDescriptor.isContainer());
		assertFalse(testClassDescriptor.isTest());
		assertEquals(testClass.getName(), testClassDescriptor.getDisplayName());
		assertEquals("junit4:" + suiteClass.getName() + "/" + testClass.getName(), testClassDescriptor.getUniqueId());

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTrue(testMethodDescriptor.isTest());
		assertFalse(testMethodDescriptor.isContainer());
		assertEquals("test", testMethodDescriptor.getDisplayName());
		assertEquals(
			"junit4:" + suiteClass.getName() + "/" + testClass.getName() + "/test(" + testClass.getName() + ")",
			testMethodDescriptor.getUniqueId());
		assertThat(testMethodDescriptor.getChildren()).isEmpty();
	}

	@Test
	void doesNotResolvePlainOldJavaClassesWithoutAnyTest() {
		Class<?> testClass = PlainOldJavaClassWithoutAnyTest.class;
		TestPlanSpecification specification = build(forClass(testClass));

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		assertThat(engineDescriptor.getChildren()).isEmpty();
	}

	@Test
	void doesNotResolveClassRunWithJUnit5() {
		Class<?> testClass = TestCaseRunWithJUnit5.class;
		TestPlanSpecification specification = build(forClass(testClass));

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		assertThat(engineDescriptor.getChildren()).isEmpty();
	}

	private static <T> T getOnlyElement(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		Preconditions.condition(iterator.hasNext(), () -> "iterable must not be empty: " + iterable);
		T result = iterator.next();
		Preconditions.condition(!iterator.hasNext(), () -> "iterable must not contain more than one item: " + iterable);
		return result;
	}
}
