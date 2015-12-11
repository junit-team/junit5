/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.Assert.*;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.engine.TestPlanSpecification.build;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.engine.junit5.resolver.ClassResolver;
import org.junit.gen5.engine.junit5.resolver.MethodResolver;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistry;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistryImpl;
import org.junit.gen5.engine.junit5.samples.*;

public class SpecificationResolverTests {
	private JUnit5TestEngine testEngine;

	@Before
	public void setUp() throws Exception {
		TestResolverRegistry testResolverRegistry = new TestResolverRegistryImpl();
		testResolverRegistry.register(new ClassResolver());
		testResolverRegistry.register(new MethodResolver());

		testEngine = new JUnit5TestEngine();
		testEngine.setTestResolverRegistry(testResolverRegistry);
	}

	@org.junit.Test
  @org.junit.Ignore
	public void testSingleClassResolution() {
		ClassSpecification specification = new ClassSpecification(TwoTestAndOneNonTestMethodsSampleClass.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testTwoClassesResolution() {
		ClassSpecification specification1 = new ClassSpecification(TwoTestAndOneNonTestMethodsSampleClass.class);
		ClassSpecification specification2 = new ClassSpecification(YourTestClass.class);

		TestPlanSpecification testPlanSpecification = build(specification1, specification2);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.YourTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test3()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test4()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testClassResolutionOfNestedClass() {
		ClassSpecification specification = new ClassSpecification(OtherTestClass.NestedTestClass.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testMethodResolution() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("test1").getDeclaringClass(),
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("test1"));

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testMethodResolutionFromInheritedMethod() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(ExtendingOtherTestSampleClass.class,
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("test1"));

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()"));
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	// TODO: Should this really throw an exception?
	public void testResolutionOfNotTestMethod() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(
			TwoTestAndOneNonTestMethodsSampleClass.class,
			TwoTestAndOneNonTestMethodsSampleClass.class.getDeclaredMethod("notATest"));

		TestPlanSpecification testPlanSpecification = build(specification);
    assertThrows(IllegalArgumentException.class, () -> testEngine.discoverTests(testPlanSpecification));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testInnerClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testMethodOfInnerClassByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	public void testNonResolvableUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification("junit5:poops-machine");

		TestPlanSpecification testPlanSpecification = build(specification);
    assertThrows(IllegalArgumentException.class, () -> testEngine.discoverTests(testPlanSpecification));
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	public void testUniqueIdOfNotTestMethod() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#notATest()");

		TestPlanSpecification testPlanSpecification = build(specification);
    assertThrows(IllegalArgumentException.class, () -> testEngine.discoverTests(testPlanSpecification));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testMethodResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testMethodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testMethodResolutionByUniqueIdWithParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)"));
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	@org.junit.Ignore
	public void testMethodResolutionByUniqueIdWithWrongParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.math.BigDecimal)");

		TestPlanSpecification testPlanSpecification = build(specification);
    assertThrows(IllegalArgumentException.class, () -> testEngine.discoverTests(testPlanSpecification));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");
		UniqueIdSpecification specification2 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()");

		// Second duplicated specification should have no effect
		TestPlanSpecification testPlanSpecification = build(specification1, specification2, specification2);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));

		TestDescriptor classFromMethod1 = descriptorByUniqueId(engineDescriptor,
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()").getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(engineDescriptor,
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()").getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testPackageResolution() {
		PackageSpecification specification = new PackageSpecification(
			"org.junit.gen5.engine.junit5.descriptor.subpackage");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		assertEquals(4, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds(engineDescriptor);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases#test1()"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases#test2()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testNestedTestResolutionFromBaseClass() {
		ClassSpecification specification = new ClassSpecification(TestCaseWithNesting.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		// engineDescriptor.allDescendants().stream().forEach(d -> System.out.println(d));

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(6, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting#testA()"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testNestedTestResolutionFromNestedTestClass() {
		ClassSpecification specification = new ClassSpecification(TestCaseWithNesting.NestedTest.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(5, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testNestedTestResolutionFromUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testNestedTestResolutionFromClass() {
		ClassSpecification specification = new ClassSpecification(
			TestCaseWithNesting.NestedTest.DoubleNestedTest.class);

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@org.junit.Test
	@org.junit.Ignore
	public void testNestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()");

		TestPlanSpecification testPlanSpecification = build(specification);
		JUnit5EngineDescriptor engineDescriptor = testEngine.discoverTests(testPlanSpecification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(3, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
	}

	private TestDescriptor descriptorByUniqueId(EngineDescriptor engineDescriptor, String id) {
		return engineDescriptor.allDescendants().stream().filter(d -> d.getUniqueId().equals(id)).findFirst().get();
	}

	private List<String> uniqueIds(EngineDescriptor engineDescriptor) {
		return engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(Collectors.toList());
	}
}
