/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.JUnit5TestDescriptorTests.StaticTestCase.StaticTestCaseLevel2;

/**
 * Unit tests for {@link ClassTestDescriptor}, {@link NestedClassTestDescriptor},
 * and {@link MethodTestDescriptor}.
 *
 * @since 5.0
 */
public class JUnit5TestDescriptorTests {

	private static final UniqueId uniqueId = UniqueId.root("enigma", "foo");

	@Test
	public void constructFromMethod() throws Exception {
		Class<?> testClass = ASampleTestCase.class;
		Method testMethod = testClass.getDeclaredMethod("test");
		MethodTestDescriptor descriptor = new MethodTestDescriptor(uniqueId, testClass, testMethod);

		assertEquals(uniqueId, descriptor.getUniqueId());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test()", descriptor.getDisplayName(), "display name:");
	}

	@Test
	public void constructFromMethodWithAnnotations() throws Exception {
		JUnit5TestDescriptor classDescriptor = new ClassTestDescriptor(uniqueId, ASampleTestCase.class);
		Method testMethod = ASampleTestCase.class.getDeclaredMethod("foo");
		MethodTestDescriptor methodDescriptor = new MethodTestDescriptor(uniqueId, ASampleTestCase.class, testMethod);
		classDescriptor.addChild(methodDescriptor);

		assertEquals(testMethod, methodDescriptor.getTestMethod());
		assertEquals("custom test name", methodDescriptor.getDisplayName(), "display name:");

		List<String> tags = methodDescriptor.getTags().stream().map(TestTag::getName).collect(Collectors.toList());
		assertEquals(4, methodDescriptor.getTags().size());
		assertTrue(tags.contains("methodTag1"));
		assertTrue(tags.contains("methodTag2"));

		// Methods "inherit" tags from their test class
		assertTrue(tags.contains("classTag1"));
		assertTrue(tags.contains("classTag2"));
	}

	@Test
	public void constructClassDescriptorWithAnnotations() throws Exception {
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, ASampleTestCase.class);

		assertEquals(ASampleTestCase.class, descriptor.getTestClass());
		assertThat(descriptor.getTags()).containsExactly(TestTag.of("classTag1"), TestTag.of("classTag2"));
	}

	@Test
	public void constructFromMethodWithCustomTestAnnotation() throws Exception {
		Method testMethod = ASampleTestCase.class.getDeclaredMethod("customTestAnnotation");
		MethodTestDescriptor descriptor = new MethodTestDescriptor(uniqueId, ASampleTestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom name", descriptor.getDisplayName(), "display name:");
		assertThat(descriptor.getTags()).containsExactly(TestTag.of("custom tag"));
	}

	@Test
	public void constructFromMethodWithParameters() throws Exception {
		Method testMethod = ASampleTestCase.class.getDeclaredMethod("test", String.class, BigDecimal.class);
		MethodTestDescriptor descriptor = new MethodTestDescriptor(uniqueId, ASampleTestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test(String, BigDecimal)", descriptor.getDisplayName(), "display name:");
	}

	@Test
	void defaultDisplayNamesForTestClasses() {
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, getClass());
		assertEquals(getClass().getSimpleName(), descriptor.getDisplayName());

		descriptor = new NestedClassTestDescriptor(uniqueId, NestedTestCase.class);
		assertEquals(NestedTestCase.class.getSimpleName(), descriptor.getDisplayName());

		descriptor = new ClassTestDescriptor(uniqueId, StaticTestCase.class);
		String staticDisplayName = getClass().getSimpleName() + "$" + StaticTestCase.class.getSimpleName();
		assertEquals(staticDisplayName, descriptor.getDisplayName());

		descriptor = new ClassTestDescriptor(uniqueId, StaticTestCaseLevel2.class);
		staticDisplayName += "$" + StaticTestCaseLevel2.class.getSimpleName();
		assertEquals(staticDisplayName, descriptor.getDisplayName());
	}

	@Tag("classTag1")
	@Tag("classTag2")
	@DisplayName("custom class name")
	@SuppressWarnings("unused")
	private static class ASampleTestCase {

		void test() {
		}

		void test(String txt, BigDecimal sum) {
		}

		@Test
		@DisplayName("custom test name")
		@Tag("methodTag1")
		@Tag("methodTag2")
		void foo() {
		}

		@CustomTestAnnotation
		void customTestAnnotation() {
		}

	}

	@Test
	@DisplayName("custom name")
	@Tag("custom tag")
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

	@Nested
	class NestedTestCase {
	}

	static class StaticTestCase {

		static class StaticTestCaseLevel2 {
		}
	}

}
