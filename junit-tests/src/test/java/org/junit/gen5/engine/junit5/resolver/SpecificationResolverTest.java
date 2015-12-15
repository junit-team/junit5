/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.gen5.engine.TestPlanSpecification.build;

import java.util.List;

import org.junit.Before;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.samples.*;

public class SpecificationResolverTest {
	private JUnit5TestEngine testEngine;

	@Before
	public void setUp() throws Exception {
		testEngine = new JUnit5TestEngine();
		testEngine.initialize();
	}

	@org.junit.Test
	public void testSingleClassResolution() {
		ClassSpecification specification = new ClassSpecification(TwoTestAndOneNonTestMethodsSampleClass.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test2()");
	}

	@org.junit.Test
	public void testTwoClassesResolution() {
		ClassSpecification specification1 = new ClassSpecification(TwoTestAndOneNonTestMethodsSampleClass.class);
		ClassSpecification specification2 = new ClassSpecification(TwoPassingTestsSampleClass.class);

		TestPlanSpecification testPlanSpecification = build(specification1, specification2);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test2()",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoPassingTestsSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoPassingTestsSampleClass#test1()",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoPassingTestsSampleClass#test2()");
	}

	@org.junit.Test
	public void testClassResolutionOfNestedClass() {
		ClassSpecification specification = new ClassSpecification(OtherTestClass.NestedTestClass.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass#test5()",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass#test6()");
	}

	@org.junit.Test
	public void testMethodResolution() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("test1").getDeclaringClass(),
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("test1"));

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()");
	}

	@org.junit.Test
	public void testMethodResolutionFromInheritedMethod() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(ExtendingOtherTestSampleClass.class,
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("test1"));

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass#test1()");
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	// TODO: Should this really throw an exception?
	public void testResolutionOfNotTestMethod() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(TwoTestAndOneNonTestMethodsSampleClass.class,
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("notATest"));

		TestPlanSpecification testPlanSpecification = build(specification);
		testEngine.discoverTests(testPlanSpecification);
	}

	@org.junit.Test
	public void testClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test2()");
	}

	@org.junit.Test
	public void testInnerClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass#test5()",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass#test6()");
	}

	@org.junit.Test
	public void testMethodOfInnerClassByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass#test5()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass",
			"junit5:org.junit.gen5.engine.junit5.samples.OtherTestClass$NestedTestClass#test5()");
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	// TODO: Should this really throw an exception?
	public void testNonResolvableUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification("junit5:poops-machine");

		TestPlanSpecification testPlanSpecification = build(specification);
		testEngine.discoverTests(testPlanSpecification);
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	// TODO: Should this really throw an exception?
	public void testUniqueIdOfNotTestMethod() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#notATest()");

		TestPlanSpecification testPlanSpecification = build(specification);
		testEngine.discoverTests(testPlanSpecification);
	}

	@org.junit.Test
	public void testMethodResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()");
	}

	@org.junit.Test
	public void testMethodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass#test1()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass#test1()");
	}

	@org.junit.Test
	public void testMethodResolutionByUniqueIdWithParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass#test7(java.lang.String)");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass#test7(java.lang.String)");
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	// TODO: Should this really throw an exception?
	public void testMethodResolutionByUniqueIdWithWrongParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.ExtendingOtherTestSampleClass#test7(java.math.BigDecimal)");

		TestPlanSpecification testPlanSpecification = build(specification);
		testEngine.discoverTests(testPlanSpecification);
	}

	@org.junit.Test
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()");
		UniqueIdSpecification specification2 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test2()");

		// Second duplicated specification should have no effect
		TestPlanSpecification testPlanSpecification = build(specification1, specification2, specification2);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()",
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test2()");

		TestDescriptor classFromMethod1 = descriptorByUniqueId(engineDescriptor,
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test1()").getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(engineDescriptor,
			"junit5:org.junit.gen5.engine.junit5.samples.TwoTestAndOneNonTestMethodsSampleClass#test2()").getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	@org.junit.Test
	public void testPackageResolution() {
		PackageSpecification specification = new PackageSpecification(
			"org.junit.gen5.engine.junit5.samples.subpackage");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.subpackage.Class1WithTestCases",
			"junit5:org.junit.gen5.engine.junit5.samples.subpackage.Class1WithTestCases#test1()",
			"junit5:org.junit.gen5.engine.junit5.samples.subpackage.Class2WithTestCases",
			"junit5:org.junit.gen5.engine.junit5.samples.subpackage.Class2WithTestCases#test2()");
	}

	@org.junit.Test
	public void testNestedTestResolutionFromBaseClass() {
		ClassSpecification specification = new ClassSpecification(TestCaseWithNesting.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting#testA()",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest#testB()",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest#testC()");
	}

	@org.junit.Test
	public void testNestedTestResolutionFromNestedTestClass() {
		ClassSpecification specification = new ClassSpecification(TestCaseWithNesting.NestedTest.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest#testB()",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest#testC()");
	}

	@org.junit.Test
	public void testNestedTestResolutionFromUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest#testC()");
	}

	@org.junit.Test
	public void testNestedTestResolutionFromClass() {
		ClassSpecification specification = new ClassSpecification(
			TestCaseWithNesting.NestedTest.DoubleNestedTest.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest$DoubleNestedTest#testC()");
	}

	@org.junit.Test
	public void testNestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest#testB()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertThat(uniqueIdsOf(engineDescriptor)).containsOnly(
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest",
			"junit5:org.junit.gen5.engine.junit5.samples.TestCaseWithNesting$NestedTest#testB()");
	}

	private TestDescriptor descriptorByUniqueId(EngineDescriptor engineDescriptor, String id) {
		return engineDescriptor.allDescendants().stream().filter(d -> d.getUniqueId().equals(id)).findFirst().get();
	}

	private List<String> uniqueIdsOf(EngineDescriptor engineDescriptor) {
		return engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(toList());
	}
}