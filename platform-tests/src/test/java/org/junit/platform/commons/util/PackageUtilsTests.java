/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.ValueWrapper;

/**
 * Unit tests for {@link PackageUtils}.
 *
 * @since 1.0
 */
class PackageUtilsTests {

	@Test
	void getAttributeWithNullType() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(null, p -> "any"));
		assertEquals("type must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithNullFunction() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(getClass(), null));
		assertEquals("function must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithFunctionReturningNullIsEmpty() {
		assertFalse(PackageUtils.getAttribute(ValueWrapper.class, p -> null).isPresent());
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
	void getAttributeWithNullClassLoader() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(null, ""));
		assertEquals("loader must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithNullName() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(getClass().getClassLoader(), null));
		assertEquals("name must not be blank", exception.getMessage());
	}

	@Test
	void getAttributeWithEmptyName() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> PackageUtils.getAttribute(getClass().getClassLoader(), ""));
		assertEquals("name must not be blank", exception.getMessage());
	}
}
