/*
 * Copyright 2015-2017 the original author or authors.
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
 * Unit tests for {@link PackageSource}, {@link ClassSource}, and
 * {@link MethodSource}.
 *
 * @since 1.0
 */
class GenericSourceTests extends AbstractTestSourceTests {

	@Test
	void packageSourceFromNullPackageName() {
		assertThrows(PreconditionViolationException.class, () -> new PackageSource((String) null));
	}

	@Test
	void packageSourceFromEmptyPackageName() {
		assertThrows(PreconditionViolationException.class, () -> new PackageSource("  "));
	}

	@Test
	void packageSourceFromPackageName() {
		String testPackage = getClass().getPackage().getName();
		PackageSource source = new PackageSource(testPackage);

		assertThat(source.getPackageName()).isEqualTo(testPackage);
	}

	@Test
	void packageSourceFromNullPackageReference() {
		assertThrows(PreconditionViolationException.class, () -> new PackageSource((Package) null));
	}

	@Test
	void packageSourceFromPackageReference() {
		Package testPackage = getClass().getPackage();
		PackageSource source = new PackageSource(testPackage);

		assertThat(source.getPackageName()).isEqualTo(testPackage.getName());
	}

	@Test
	void classNameSource() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		ClassSource source = new ClassSource(testClassName);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classNameSourceWithFilePosition() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		FilePosition position = new FilePosition(42, 23);
		ClassSource source = new ClassSource(testClassName, position);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void classSource() {
		Class<?> testClass = getClass();
		ClassSource source = new ClassSource(testClass);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classSourceWithFilePosition() {
		Class<?> testClass = getClass();
		FilePosition position = new FilePosition(42, 23);
		ClassSource source = new ClassSource(testClass, position);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void methodSource() throws Exception {
		Method testMethod = getMethod("method1");
		MethodSource source = new MethodSource(testMethod);

		assertThat(source.getClassName()).isEqualTo(getClass().getName());
		assertThat(source.getMethodName()).isEqualTo(testMethod.getName());
		assertThat(source.getMethodParameterTypes()).isEqualTo(String.class.getName());
	}

	@Test
	void equalsAndHashCodeForPackageSource() {
		Package pkg1 = getClass().getPackage();
		Package pkg2 = String.class.getPackage();
		assertEqualsAndHashCode(new PackageSource(pkg1), new PackageSource(pkg1), new PackageSource(pkg2));
	}

	@Test
	void equalsAndHashCodeForClassSource() {
		Class<?> class1 = String.class;
		Class<?> class2 = Number.class;
		assertEqualsAndHashCode(new ClassSource(class1), new ClassSource(class1), new ClassSource(class2));
	}

	@Test
	void equalsAndHashCodeForMethodSource(TestInfo testInfo) throws Exception {
		Method method1 = getMethod("method1");
		Method method2 = getMethod("method2");
		assertEqualsAndHashCode(new MethodSource(method1), new MethodSource(method1), new MethodSource(method2));
	}

	private Method getMethod(String name) throws Exception {
		return getClass().getDeclaredMethod(name, String.class);
	}

	void method1(String text) {
	}

	void method2(String text) {
	}

}
