/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Context;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.PackageSpecification;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueIdSpecification;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;

public class SpecificationResolverTest {

	private final EngineDescriptor engineDescriptor = new EngineDescriptor(new JUnit5TestEngine());
	private SpecificationResolver resolver = new SpecificationResolver(engineDescriptor);

	@org.junit.Test
	public void testSingleClassNameResolution() {
		ClassNameSpecification specification = new ClassNameSpecification(MyTestClass.class.getName());

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@org.junit.Test
	public void testClassNameResolutionOfNestedClass() {
		ClassNameSpecification specification = new ClassNameSpecification(
			OtherTestClass.NestedTestClass.class.getName());

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allChildren().size());
		List<String> uniqueIds = engineDescriptor.allChildren().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@org.junit.Test
	public void testTwoClassNameResolution() {
		ClassNameSpecification specification1 = new ClassNameSpecification(MyTestClass.class.getName());
		ClassNameSpecification specification2 = new ClassNameSpecification(YourTestClass.class.getName());

		resolver.resolveElement(specification1);
		resolver.resolveElement(specification2);

		assertEquals(6, engineDescriptor.allChildren().size());
		List<String> uniqueIds = engineDescriptor.allChildren().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.YourTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test3()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test4()"));
	}

	@org.junit.Test
	public void testClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass");

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@org.junit.Test
	public void testInnerClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass");

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@org.junit.Test
	public void testMethodOfInnerClassByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	public void testNonResolvableUniqueId() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification("junit5:poops-machine");

		resolver.resolveElement(specification1);
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	public void testUniqueIdOfNotTestMethod() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#notATest()");
		resolver.resolveElement(specification1);
	}

	@org.junit.Test
	public void testMethodResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
	}

	@org.junit.Test
	public void testMethodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()"));
	}

	@org.junit.Test
	public void testMethodResolutionByUniqueIdWithParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)"));
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	public void testMethodResolutionByUniqueIdWithWrongParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.math.BigDecimal)");

		resolver.resolveElement(specification);
	}

	@org.junit.Test
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");
		UniqueIdSpecification specification2 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()");

		resolver.resolveElement(specification1);
		resolver.resolveElement(specification2);
		resolver.resolveElement(specification2); // should have no effect

		assertEquals(3, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));

		TestDescriptor classFromMethod1 = descriptorByUniqueId(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()").getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(
			"junit5:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()").getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	@org.junit.Test
	public void testPackageResolution() {
		PackageSpecification specification = new PackageSpecification(
			"org.junit.gen5.engine.junit5.descriptor.subpackage");
		resolver.resolveElement(specification);

		assertEquals(4, engineDescriptor.allChildren().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases#test1()"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases#test2()"));
	}

	@org.junit.Test
	public void testContextResolutionFromContainerClass() {
		ClassNameSpecification specification = new ClassNameSpecification(TestCaseWithContexts.class.getName());

		resolver.resolveElement(specification);

		List<String> uniqueIds = engineDescriptor.allChildren().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(6, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts"));
		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts#testA()"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext#testB()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext@InnerInnerContext"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext@InnerInnerContext#testC()"));
	}

	@org.junit.Test
	public void testContextResolutionFromContextClass() {
		ClassNameSpecification specification = new ClassNameSpecification(
			TestCaseWithContexts.InnerContext.class.getName());

		resolver.resolveElement(specification);

		List<String> uniqueIds = engineDescriptor.allChildren().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(5, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext#testB()"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext@InnerInnerContext"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext@InnerInnerContext#testC()"));
	}

	@org.junit.Test
	public void testContextResolutionFromUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext@InnerInnerContext");

		resolver.resolveElement(specification);

		List<String> uniqueIds = engineDescriptor.allChildren().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext@InnerInnerContext"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext@InnerInnerContext#testC()"));
	}

	@org.junit.Test
	public void testContextResolutionFromUniqueIdToMethod() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext#testB()");

		resolver.resolveElement(specification);

		//engineDescriptor.allChildren().forEach(id -> System.out.println(id));

		List<String> uniqueIds = engineDescriptor.allChildren().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(3, uniqueIds.size());

		assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts"));
		assertTrue(
			uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext"));
		assertTrue(uniqueIds.contains(
			"junit5:org.junit.gen5.engine.junit5.descriptor.TestCaseWithContexts@InnerContext#testB()"));
	}

	private TestDescriptor descriptorByUniqueId(String id) {
		return engineDescriptor.allChildren().stream().filter(d -> d.getUniqueId().equals(id)).findFirst().get();
	}

	private List<String> uniqueIds() {
		return engineDescriptor.allChildren().stream().map(d -> d.getUniqueId()).collect(Collectors.toList());
	}

}

class MyTestClass {

	@Test
	void test1() {

	}

	@Test
	void test2() {

	}

	void notATest() {

	}
}

class YourTestClass {

	@Test
	void test3() {

	}

	@Test
	void test4() {

	}

}

class HerTestClass extends MyTestClass {

	@Test
	void test7(String param) {

	}
}

class OtherTestClass {

	static class NestedTestClass {

		@Test
		void test5() {

		}

		@Test
		void test6() {

		}

	}
}

class TestCaseWithContexts {

	@Test
	void testA() {

	}

	@Context
	class InnerContext {

		@Test
		void testB() {

		}

		@Context
		class InnerInnerContext {

			@Test
			void testC() {

			}

		}

	}
}
