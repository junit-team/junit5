/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;

/**
 * Unit tests for {@link JavaPackageSource}, {@link JavaClassSource}, and
 * {@link JavaMethodSource}.
 *
 * @since 5.0
 */
class JavaSourceTests {

	@Test
	void packageSource() {
		Package testPackage = JavaSourceTests.class.getPackage();
		JavaPackageSource source = new JavaPackageSource(testPackage);

		assertThat(source.getPackageName()).isEqualTo(testPackage.getName());
	}

	@Test
	void classSource() {
		Class<?> testClass = JavaSourceTests.class;
		JavaClassSource source = new JavaClassSource(testClass);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
	}

	@Test
	void methodSource(TestInfo testInfo) throws Exception {
		Class<?> testClass = JavaSourceTests.class;
		String testName = testInfo.getDisplayName();
		Method testMethod = testClass.getDeclaredMethod(testName, TestInfo.class);
		JavaMethodSource source = new JavaMethodSource(testMethod);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getJavaMethodName()).isEqualTo(testName);
		assertThat(source.getJavaMethodParameterTypes()).containsExactly(TestInfo.class);
	}

}
