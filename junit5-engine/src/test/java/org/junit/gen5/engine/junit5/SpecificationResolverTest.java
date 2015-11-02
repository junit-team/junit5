/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueIdSpecification;
import org.junit.gen5.engine.junit5.descriptor.SpecificationResolver;

public class SpecificationResolverTest {

	private Set<TestDescriptor> descriptors;
	private EngineDescriptor engineDescriptor;
	private SpecificationResolver resolver;

	@org.junit.Before
	public void init() {
		descriptors = new HashSet<>();
		engineDescriptor = new EngineDescriptor(new JUnit5TestEngine());
		resolver = new SpecificationResolver(descriptors, engineDescriptor);
	}

	@org.junit.Test
	public void testSingleClassNameResolution() {
		ClassNameSpecification specification = new ClassNameSpecification(MyTestClass.class.getName());

		resolver.resolveElement(specification);

		Assert.assertEquals(3, descriptors.size());
		Set uniqueIds = descriptors.stream().map(d -> d.getUniqueId()).collect(Collectors.toSet());
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test2()"));
	}

	@org.junit.Test
	public void testTwoClassNameResolution() {
		ClassNameSpecification specification1 = new ClassNameSpecification(MyTestClass.class.getName());
		ClassNameSpecification specification2 = new ClassNameSpecification(YourTestClass.class.getName());

		resolver.resolveElement(specification1);
		resolver.resolveElement(specification2);

		Assert.assertEquals(6, descriptors.size());
		Set uniqueIds = descriptors.stream().map(d -> d.getUniqueId()).collect(Collectors.toSet());
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test2()"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.YourTestClass"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.YourTestClass#test3()"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.YourTestClass#test4()"));
	}

	@org.junit.Test
	public void testClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.MyTestClass");

		resolver.resolveElement(specification);

		Assert.assertEquals(3, descriptors.size());
		Set uniqueIds = descriptors.stream().map(d -> d.getUniqueId()).collect(Collectors.toSet());
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test2()"));
	}

	@org.junit.Test(expected = IllegalArgumentException.class)
	public void testNonResolvableUniqueId() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification("junit5:poops-machine");

		resolver.resolveElement(specification1);
	}

	@org.junit.Test
	public void testMethodResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()");

		resolver.resolveElement(specification);

		Assert.assertEquals(2, descriptors.size());
		Set uniqueIds = descriptors.stream().map(d -> d.getUniqueId()).collect(Collectors.toSet());
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()"));
	}

	@org.junit.Test
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()");
		UniqueIdSpecification specification2 = new UniqueIdSpecification(
			"junit5:org.junit.gen5.engine.junit5.MyTestClass#test2()");

		resolver.resolveElement(specification1);
		resolver.resolveElement(specification2);

		Assert.assertEquals(3, descriptors.size());
		Set uniqueIds = descriptors.stream().map(d -> d.getUniqueId()).collect(Collectors.toSet());
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()"));
		Assert.assertTrue(uniqueIds.contains("junit5:org.junit.gen5.engine.junit5.MyTestClass#test2()"));

		TestDescriptor fromMethod1 = descriptorByUniqueId(
			"junit5:org.junit.gen5.engine.junit5.MyTestClass#test1()").getParent();
		TestDescriptor fromMethod2 = descriptorByUniqueId(
			"junit5:org.junit.gen5.engine.junit5.MyTestClass#test2()").getParent();

		Assert.assertEquals(fromMethod1, fromMethod2);
		//		Assert.assertSame(fromMethod1, fromMethod2);
	}

	public TestDescriptor descriptorByUniqueId(String id) {
		return descriptors.stream().filter(d -> {
			return d.getUniqueId().equals(id);
		}).findFirst().get();
	}
}

class MyTestClass {

	@Test
	void test1() {

	}

	@Test
	void test2() {

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