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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link ClassLoaderUtils}.
 *
 * @since 1.0
 */
class ClassLoaderUtilsTests {

	@Test
	void getDefaultClassLoaderWithExplicitContextClassLoader() {
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		ClassLoader mock = mock(ClassLoader.class);
		Thread.currentThread().setContextClassLoader(mock);
		try {
			assertSame(mock, ClassLoaderUtils.getDefaultClassLoader());
		}
		finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	@Test
	void getDefaultClassLoaderWithNullContextClassLoader() {
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(null);
		try {
			assertSame(ClassLoader.getSystemClassLoader(), ClassLoaderUtils.getDefaultClassLoader());
		}
		finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	@Test
	void getLocationFromNullFails() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> ClassLoaderUtils.getLocation(null));
		assertEquals("object must not be null", exception.getMessage());
	}

	@Test
	void getLocationFromVariousObjectsArePresent() {
		assertTrue(ClassLoaderUtils.getLocation(void.class).isPresent());
		assertTrue(ClassLoaderUtils.getLocation(byte.class).isPresent());
		assertTrue(ClassLoaderUtils.getLocation(this).isPresent());
		assertTrue(ClassLoaderUtils.getLocation("").isPresent());
		assertTrue(ClassLoaderUtils.getLocation(0).isPresent());
		assertTrue(ClassLoaderUtils.getLocation(Thread.State.RUNNABLE).isPresent());
	}

}
