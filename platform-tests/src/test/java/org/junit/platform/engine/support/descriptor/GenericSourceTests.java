/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link PackageSource}, {@link ClassSource}, and
 * {@link MethodSource}.
 *
 * @since 1.0
 */
class GenericSourceTests extends AbstractTestSourceTests {

	@Test
	void packageSourceFromNullPackageName() {
		assertThrows(PreconditionViolationException.class, () -> PackageSource.from((String) null));
	}

	@Test
	void packageSourceFromEmptyPackageName() {
		assertThrows(PreconditionViolationException.class, () -> PackageSource.from("  "));
	}

	@Test
	void packageSourceFromPackageName() {
		String testPackage = getClass().getPackage().getName();
		PackageSource source = PackageSource.from(testPackage);

		assertThat(source.getPackageName()).isEqualTo(testPackage);
	}

	@Test
	void packageSourceFromNullPackageReference() {
		assertThrows(PreconditionViolationException.class, () -> PackageSource.from((Package) null));
	}

	@Test
	void packageSourceFromPackageReference() {
		Package testPackage = getClass().getPackage();
		PackageSource source = PackageSource.from(testPackage);

		assertThat(source.getPackageName()).isEqualTo(testPackage.getName());
	}

	@Test
	void classNameSource() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		ClassSource source = ClassSource.from(testClassName);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classNameSourceWithFilePosition() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		FilePosition position = FilePosition.from(42, 23);
		ClassSource source = ClassSource.from(testClassName, position);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void classSource() {
		Class<?> testClass = getClass();
		ClassSource source = ClassSource.from(testClass);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classSourceWithFilePosition() {
		Class<?> testClass = getClass();
		FilePosition position = FilePosition.from(42, 23);
		ClassSource source = ClassSource.from(testClass, position);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void methodSource() throws Exception {
		Method testMethod = getMethod("method1");
		MethodSource source = MethodSource.from(testMethod);

		assertThat(source.getClassName()).isEqualTo(getClass().getName());
		assertThat(source.getMethodName()).isEqualTo(testMethod.getName());
		assertThat(source.getMethodParameterTypes()).isEqualTo(String.class.getName());
	}

	@Test
	void equalsAndHashCodeForPackageSource() {
		Package pkg1 = getClass().getPackage();
		Package pkg2 = String.class.getPackage();
		assertEqualsAndHashCode(PackageSource.from(pkg1), PackageSource.from(pkg1), PackageSource.from(pkg2));
	}

	@Test
	void equalsAndHashCodeForClassSource() {
		Class<?> class1 = String.class;
		Class<?> class2 = Number.class;
		assertEqualsAndHashCode(ClassSource.from(class1), ClassSource.from(class1), ClassSource.from(class2));
	}

	@Test
	void equalsAndHashCodeForMethodSource(TestInfo testInfo) throws Exception {
		Method method1 = getMethod("method1");
		Method method2 = getMethod("method2");
		assertEqualsAndHashCode(MethodSource.from(method1), MethodSource.from(method1), MethodSource.from(method2));
	}

	private Method getMethod(String name) throws Exception {
		return getClass().getDeclaredMethod(name, String.class);
	}

	void method1(String text) {
	}

	void method2(String text) {
	}

}
