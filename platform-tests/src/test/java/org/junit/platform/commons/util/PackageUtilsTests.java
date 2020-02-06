/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.lang.model.SourceVersion;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.PackageUtils.JavaNameUtils;
import org.opentest4j.ValueWrapper;

/**
 * Unit tests for {@link PackageUtils}.
 *
 * @since 1.0
 */
class PackageUtilsTests {

	@Test
	void assertPackageNameIsValidForValidPackageNames() {
		PackageUtils.assertPackageNameIsValid(""); // default package
		PackageUtils.assertPackageNameIsValid("non.existing.but.all.segments.are.syntactically.valid");
	}

	@Test
	void assertPackageNameIsValidForNullPackageName() {
		assertThrows(PreconditionViolationException.class, () -> PackageUtils.assertPackageNameIsValid(null));
	}

	@Test
	void assertPackageNameIsValidForWhitespacePackageName() {
		assertThrows(PreconditionViolationException.class, () -> PackageUtils.assertPackageNameIsValid("    "));
	}

	@Test
	void assertPackageNameIsValidForInvalidPackageNames() {
		assertThrows(PreconditionViolationException.class, () -> PackageUtils.assertPackageNameIsValid(".a"));
		assertThrows(PreconditionViolationException.class, () -> PackageUtils.assertPackageNameIsValid("a."));
		assertThrows(PreconditionViolationException.class, () -> PackageUtils.assertPackageNameIsValid("a..b"));
		assertThrows(PreconditionViolationException.class, () -> PackageUtils.assertPackageNameIsValid("byte.true"));
	}

	@Test
	void getAttributeWithNullType() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(null, p -> "any"));
		assertEquals("type must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithNullFunction() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(getClass(), (Function<Package, String>) null));
		assertEquals("function must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithFunctionReturningNullIsEmpty() {
		assertFalse(PackageUtils.getAttribute(ValueWrapper.class, p -> null).isPresent());
	}

	@Test
	void getAttributeFromDefaultPackageMemberIsEmpty() throws Exception {
		Class<?> classInDefaultPackage = ReflectionUtils.tryToLoadClass("DefaultPackageTestCase").get();
		assertFalse(PackageUtils.getAttribute(classInDefaultPackage, Package::getSpecificationTitle).isPresent());
	}

	@TestFactory
	List<DynamicTest> attributesFromValueWrapperClassArePresent() {
		return Arrays.asList(dynamicTest("getName", isPresent(Package::getName)),
			dynamicTest("getImplementationTitle", isPresent(Package::getImplementationTitle)),
			dynamicTest("getImplementationVendor", isPresent(Package::getImplementationVendor)),
			dynamicTest("getImplementationVersion", isPresent(Package::getImplementationVersion)),
			dynamicTest("getSpecificationTitle", isPresent(Package::getSpecificationTitle)),
			dynamicTest("getSpecificationVendor", isPresent(Package::getSpecificationVendor)),
			dynamicTest("getSpecificationVersion", isPresent(Package::getSpecificationVersion)));
	}

	private Executable isPresent(Function<Package, String> function) {
		return () -> assertTrue(PackageUtils.getAttribute(ValueWrapper.class, function).isPresent());
	}

	@Test
	void getAttributeWithNullTypeAndName() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(null, "foo"));
		assertEquals("type must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithNullName() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(getClass(), (String) null));
		assertEquals("name must not be blank", exception.getMessage());
	}

	@Test
	void getAttributeWithEmptyName() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(getClass(), ""));
		assertEquals("name must not be blank", exception.getMessage());
	}

	@Nested
	class JavaNameUtilsTests {

		@ParameterizedTest
		@ValueSource(strings = { "name", "_NAME", "null", "var", "public", "$+!", "", "  ", "123", "true" })
		void isJavaName(String s) {
			assertEquals(SourceVersion.isName(s), JavaNameUtils.isJavaName(s));
		}

		@Test
		void isJavaNameWithNullValue() {
			// According to https://bugs.openjdk.java.net/browse/JDK-6362039,
			// SourceVersion.isName(null) throws a NullPointerException instead
			// of returning false.
			assertThrows(NullPointerException.class, () -> SourceVersion.isName(null));

			// However, for our purposes, we return false for a null value.
			assertFalse(JavaNameUtils.isJavaName(null));
		}

	}

}
