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

import static org.junit.gen5.api.Assertions.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Name;
import org.junit.gen5.api.Tag;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestTag;

/**
 * Unit tests for {@link MethodTestDescriptor}.
 *
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
@Tag("classTag1")
@Tag("classTag2")
@Name("custom class name")
public class MethodTestDescriptorTests {

	@org.junit.Test
	public void constructFromMethod() throws Exception {
		Class<?> testClass = getClass();
		Method testMethod = testClass.getDeclaredMethod("test");
		MethodTestDescriptor descriptor = new MethodTestDescriptor("a method id", testMethod);

		assertEquals("a method id", descriptor.getUniqueId());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithAnnotations() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("foo");
		MethodTestDescriptor descriptor = new MethodTestDescriptor("any id", testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom test name", descriptor.getDisplayName(), "display name:");

		List<String> tags = descriptor.getTags().stream().map(TestTag::getName).collect(Collectors.toList());
		assertEquals(2, descriptor.getTags().size());
		assertTrue(tags.contains("methodTag1"));
		assertTrue(tags.contains("methodTag2"));

	}

	@org.junit.Test
	public void constructClassDescriptorWithAnnotations() throws Exception {
		ClassTestDescriptor descriptor = new ClassTestDescriptor("any id", getClass());

		assertEquals(getClass(), descriptor.getTestClass());
		assertEquals("custom class name", descriptor.getDisplayName(), "display name:");

		List<String> tags = descriptor.getTags().stream().map(TestTag::getName).collect(Collectors.toList());
		assertEquals(2, descriptor.getTags().size());
		assertTrue(tags.contains("classTag1"));
		assertTrue(tags.contains("classTag2"));

	}

	@org.junit.Test
	public void constructFromMethodWithCustomTestAnnotation() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("customTestAnnotation");
		MethodTestDescriptor descriptor = new MethodTestDescriptor("any id", testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom name", descriptor.getDisplayName(), "display name:");
		List<String> tags = descriptor.getTags().stream().map(TestTag::getName).collect(Collectors.toList());
		assertEquals("custom tag", tags.get(0), "tags:");
	}

	@org.junit.Test
	public void constructFromMethodWithParameters() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("test", String.class, BigDecimal.class);
		MethodTestDescriptor descriptor = new MethodTestDescriptor("any id", testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	void test() {
	}

	void test(String txt, BigDecimal sum) {
	}

	@Test
	@Name("custom test name")
	@Tag("methodTag1")
	@Tag("methodTag2")
	void foo() {
	}

	@CustomTestAnnotation
	void customTestAnnotation() {
	}

	@Test
	@Name("custom name")
	@Tag("custom tag")
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

}
