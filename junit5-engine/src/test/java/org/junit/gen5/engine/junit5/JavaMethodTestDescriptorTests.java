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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueIdSpecification;
import org.junit.gen5.engine.junit5.stubs.TestEngineDummy;

/**
 * Unit tests for {@link MethodTest}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class JavaMethodTestDescriptorTests {

	private static final String TEST_METHOD_ID = JavaMethodTestDescriptorTests.class.getName() + "#test()";
	private static final String TEST_METHOD_UID = TestEngineDummy.TEST_ENGINE_DUMMY_ID + ":" + TEST_METHOD_ID;
	private static final String TEST_METHOD_STRING_BIGDECIMAL_ID = JavaMethodTestDescriptorTests.class.getName()
			+ "#test(java.lang.String, java.math.BigDecimal)";
	private static final String TEST_METHOD_STRING_BIGDECIMAL_UID = TestEngineDummy.TEST_ENGINE_DUMMY_ID + ":"
			+ TEST_METHOD_STRING_BIGDECIMAL_ID;
	private static final EngineTestGroup ENGINE_DESCRIPTOR = new EngineTestGroup(new TestEngineDummy());


	@org.junit.Test
	public void constructFromMethod() throws Exception {
		Class<?> testClass = getClass();
		Method testMethod = testClass.getDeclaredMethod("test");
		ClassTestGroup parent = new ClassTestGroup(ENGINE_DESCRIPTOR, testClass);
		MethodTest descriptor = new MethodTest(parent, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEquals(TEST_METHOD_UID, descriptor.getUniqueId());
		assertEquals(testClass, descriptor.getParent().getTestClass());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithCustomDisplayName() throws Exception {
		Class<?> testClass = getClass();
		Method testMethod = testClass.getDeclaredMethod("foo");
		ClassTestGroup parent = new ClassTestGroup(ENGINE_DESCRIPTOR, testClass);
		MethodTest descriptor = new MethodTest(parent, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEquals(testClass, descriptor.getParent().getTestClass());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom test name", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithParameters() throws Exception {
		Class<?> testClass = getClass();
		Method testMethod = testClass.getDeclaredMethod("test", String.class, BigDecimal.class);
		ClassTestGroup parent = new ClassTestGroup(ENGINE_DESCRIPTOR, testClass);
		MethodTest descriptor = new MethodTest(parent, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEquals(TEST_METHOD_STRING_BIGDECIMAL_UID, descriptor.getUniqueId());
		assertEquals(testClass, descriptor.getParent().getTestClass());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void fromTestDescriptorIdForMethod() throws Exception {
		UniqueIdSpecificationResolver resolver = new UniqueIdSpecificationResolver();
		UniqueIdSpecification specification = new UniqueIdSpecification(TEST_METHOD_UID);
		TestDescriptor descriptor = resolver.resolve(ENGINE_DESCRIPTOR, specification);
		List<TestDescriptor> descriptors = resolver.resolveChildren(descriptor, specification);
		descriptor = descriptors.get(0);

		assertNotNull(descriptor, "descriptor:");
		assertEquals("test", descriptor.getDisplayName(), "display name:");
		assertEquals(JavaMethodTestDescriptorTests.class, ((ClassTestGroup) descriptor.getParent()).getTestClass());
		assertEquals(JavaMethodTestDescriptorTests.class.getDeclaredMethod("test"),
			((MethodTest) descriptor).getTestMethod());
	}

	@org.junit.Test
	public void fromTestDescriptorIdForMethodWithParameters() throws Exception {
		UniqueIdSpecificationResolver resolver = new UniqueIdSpecificationResolver();
		UniqueIdSpecification specification = new UniqueIdSpecification(TEST_METHOD_STRING_BIGDECIMAL_UID);
		TestDescriptor descriptor = resolver.resolve(ENGINE_DESCRIPTOR, specification);
		List<TestDescriptor> descriptors = resolver.resolveChildren(descriptor, specification);
		descriptor = descriptors.get(0);

		assertNotNull(descriptor, "descriptor:");
		assertEquals("test", descriptor.getDisplayName(), "display name:");
		assertEquals(getClass(), ((ClassTestGroup) descriptor.getParent()).getTestClass());
		assertEquals(getClass().getDeclaredMethod("test", String.class, BigDecimal.class),
			((MethodTest) descriptor).getTestMethod());
	}

	void test() {
	}

	void test(String txt, BigDecimal sum) {
	}

	@Test(name = "custom test name")
	void foo() {
	}

}
