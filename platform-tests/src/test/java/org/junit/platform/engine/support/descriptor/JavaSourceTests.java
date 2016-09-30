/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link JavaPackageSource}, {@link JavaClassSource}, and
 * {@link JavaMethodSource}.
 *
 * @since 1.0
 */
class JavaSourceTests extends AbstractTestSourceTests {

	@Test
	void packageSourceFromNullPackageName() {
		assertThrows(PreconditionViolationException.class, () -> new JavaPackageSource((String) null));
	}

	@Test
	void packageSourceFromEmptyPackageName() {
		assertThrows(PreconditionViolationException.class, () -> new JavaPackageSource("  "));
	}

	@Test
	void packageSourceFromPackageName() {
		String testPackage = getClass().getPackage().getName();
		JavaPackageSource source = new JavaPackageSource(testPackage);

		assertThat(source.getPackageName()).isEqualTo(testPackage);
	}

	@Test
	void packageSourceFromNullPackageReference() {
		assertThrows(PreconditionViolationException.class, () -> new JavaPackageSource((Package) null));
	}

	@Test
	void packageSourceFromPackageReference() {
		Package testPackage = getClass().getPackage();
		JavaPackageSource source = new JavaPackageSource(testPackage);

		assertThat(source.getPackageName()).isEqualTo(testPackage.getName());
	}

	@Test
	void classNameSource() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		JavaClassSource source = new JavaClassSource(testClassName);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classNameSourceWithFilePosition() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		FilePosition position = new FilePosition(42, 23);
		JavaClassSource source = new JavaClassSource(testClassName, position);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void classSource() {
		Class<?> testClass = getClass();
		JavaClassSource source = new JavaClassSource(testClass);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classSourceWithFilePosition() {
		Class<?> testClass = getClass();
		FilePosition position = new FilePosition(42, 23);
		JavaClassSource source = new JavaClassSource(testClass, position);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void methodSource() throws Exception {
		Method testMethod = getMethod("method1");
		JavaMethodSource source = new JavaMethodSource(testMethod);

		assertThat(source.getClassName()).isEqualTo(getClass().getName());
		assertThat(source.getMethodName()).isEqualTo(testMethod.getName());
		assertThat(source.getMethodParameterTypes()).isEqualTo(String.class.getName());
	}

	@Test
	void equalsAndHashCodeForJavaPackageSource() {
		Package pkg1 = getClass().getPackage();
		Package pkg2 = String.class.getPackage();
		assertEqualsAndHashCode(new JavaPackageSource(pkg1), new JavaPackageSource(pkg1), new JavaPackageSource(pkg2));
	}

	@Test
	void equalsAndHashCodeForJavaClassSource() {
		Class<?> class1 = String.class;
		Class<?> class2 = Number.class;
		assertEqualsAndHashCode(new JavaClassSource(class1), new JavaClassSource(class1), new JavaClassSource(class2));
	}

	@Test
	void equalsAndHashCodeForJavaMethodSource(TestInfo testInfo) throws Exception {
		Method method1 = getMethod("method1");
		Method method2 = getMethod("method2");
		assertEqualsAndHashCode(new JavaMethodSource(method1), new JavaMethodSource(method1),
			new JavaMethodSource(method2));
	}

	private Method getMethod(String name) throws Exception {
		return getClass().getDeclaredMethod(name, String.class);
	}

	void method1(String text) {
	}

	void method2(String text) {
	}

}
