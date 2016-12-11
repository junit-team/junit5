/*
 * Copyright 2015-2016 the original author or authors.
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

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PackageUtils}.
 *
 * @since 1.0
 */
class PackageUtilsTests {

	@Test
	void getAttributeWithNullType() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> {
			PackageUtils.getAttribute(null, null);
		});
		assertEquals("type must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithNullFunction() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> {
			PackageUtils.getAttribute(getClass(), null);
		});
		assertEquals("function must not be null", exception.getMessage());
	}

	@Test
	void getAttributeWithFunctionReturningNullIsEmpty() {
		assertFalse(PackageUtils.getAttribute(Object.class, p -> null).isPresent());
	}

	@Test
	void vendorFromObjectClassIsPresent() {
		assertTrue(PackageUtils.getAttribute(Object.class, Package::getSpecificationVendor).isPresent());
		assertTrue(PackageUtils.getAttribute(Object.class, Package::getImplementationVendor).isPresent());
	}

	@Test
	void versionSystemPropertyEqualsRuntimeClassImplementationVersion() {
		Supplier<AssertionError> error = () -> new AssertionError("implementation version not available");
		String actual = PackageUtils.getAttribute(Runtime.class, Package::getImplementationVersion).orElseThrow(error);
		assertEquals(System.getProperty("java.version"), actual);
	}

}
