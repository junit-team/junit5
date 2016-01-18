/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;
import org.junit.gen5.engine.support.descriptor.JavaSource;

class JavaSourceTests {

	@Test
	void classSource() {
		Class<JavaSourceTests> testClass = JavaSourceTests.class;
		JavaSource source = new JavaSource(testClass);

		assertTrue(source.isJavaClass());
		assertFalse(source.isJavaMethod());
		assertFalse(source.isDirectory());
		assertFalse(source.isFile());
		assertFalse(source.isFilePosition());

		assertThat(source.getJavaClass()).hasValue(testClass);
		assertThat(source.getJavaMethodName()).isEmpty();
		assertThat(source.getJavaMethodParameterTypes()).isEmpty();

		assertEquals(testClass.getName(), source.toString());
	}

	@Test
	void methodSource(TestInfo testInfo) throws Exception {
		Class<JavaSourceTests> testClass = JavaSourceTests.class;
		final String testName = testInfo.getDisplayName();
		Method testMethod = testClass.getDeclaredMethod(testName, TestInfo.class);
		JavaSource source = new JavaSource(testMethod);

		assertTrue(source.isJavaMethod());
		assertFalse(source.isJavaClass());
		assertFalse(source.isDirectory());
		assertFalse(source.isFile());
		assertFalse(source.isFilePosition());

		assertThat(source.getJavaClass()).hasValue(testClass);
		assertThat(source.getJavaMethodName()).hasValue(testName);
		assertThat(source.getJavaMethodParameterTypes().get()).containsExactly(TestInfo.class);

		assertEquals(testClass.getName() + "#" + testName + "(" + TestInfo.class.getName() + ")", source.toString());
	}

}
