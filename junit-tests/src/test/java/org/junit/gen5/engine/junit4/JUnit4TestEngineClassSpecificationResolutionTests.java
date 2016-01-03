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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.gen5.junit4runner.JUnit5.Classes;
import org.junit.runner.RunWith;

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

	public static class PlainJUnit4TestCaseWithSingleTestWhichFails {

		@org.junit.Test
		public void test() {
			Assert.fail("this test should fail");
		}

	}

	@Ignore
	public static class IgnoredJUnit4TestCase {

		@org.junit.Test
		public void test() {
			Assert.fail("this test is not even discovered");
		}

	}

	@RunWith(Theories.class)
	public static class SingleFailingTheoryTestCase {

		@Theory
		public void theory() {
			Assert.fail("this theory should fail");
		}

	}

	public static class PlainJUnit3TestCaseWithSingleTestWhichFails extends TestCase {

		public void test() {
			Assert.fail("this test should fail");
		}

	}

	public static class JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails extends TestCase {

		public static junit.framework.Test suite() {
			TestSuite suite = new TestSuite();
			suite.addTestSuite(PlainJUnit3TestCaseWithSingleTestWhichFails.class);
			return suite;
		}

	}

	public static class PlainOldJavaClassWithoutAnyTest {

		public void doSomething() {
			// no-op
		}

	}

	@RunWith(JUnit5.class)
	@Classes(PlainJUnit4TestCaseWithSingleTestWhichFails.class)
	public static class TestCaseRunWithJUnit5 {
	}

	private static <T> T getOnlyElement(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		Preconditions.condition(iterator.hasNext(), () -> "iterable must not be empty: " + iterable);
		T result = iterator.next();
		Preconditions.condition(!iterator.hasNext(), () -> "iterable must not contain more than one item: " + iterable);
		return result;
	}
}
