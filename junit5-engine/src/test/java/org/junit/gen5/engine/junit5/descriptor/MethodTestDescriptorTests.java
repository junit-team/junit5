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

import org.junit.gen5.api.Name;
import org.junit.gen5.api.Test;

/**
 * Unit tests for {@link MethodTestDescriptor}.
 *
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
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
	public void constructFromMethodWithCustomDisplayName() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("foo");
		MethodTestDescriptor descriptor = new MethodTestDescriptor("any id", testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom test name", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithCustomDisplayNameInCustomTestAnnotation() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("customTestAnnotation");
		MethodTestDescriptor descriptor = new MethodTestDescriptor("any id", testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom name", descriptor.getDisplayName(), "display name:");
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
	void foo() {
	}

	@CustomTestAnnotation
	void customTestAnnotation() {
	}

	@Test
	@Name("custom name")
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

}
