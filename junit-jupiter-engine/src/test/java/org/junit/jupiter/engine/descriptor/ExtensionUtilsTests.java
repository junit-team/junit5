/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.engine.descriptor.ExtensionUtils.IsNonStaticExtensionField;
import org.junit.jupiter.engine.descriptor.ExtensionUtils.IsStaticExtensionField;

/**
 * Unit tests for {@link ExtensionUtils}.
 *
 * @since 5.1
 */
class ExtensionUtilsTests {

	private final Predicate<Field> isStaticExtension = new IsStaticExtensionField();
	private final Predicate<Field> isInstanceExtension = new IsNonStaticExtensionField();

	static Object staticObject = new Object();
	Object instanceObject = new Object();

	@SuppressWarnings("unused")
	private static Extension privateStaticExtension = new DummyExtension();
	@SuppressWarnings("unused")
	private Extension privateInstanceExtension = new DummyExtension();

	static Extension staticExtension = new DummyExtension();
	Extension instanceExtension = new DummyExtension();

	@Test
	void isStaticExtension() {
		assertThat(isStaticExtension).accepts(field("staticExtension"));
	}

	@TestFactory
	Stream<DynamicTest> isNotStaticExtension() {
		return Stream.of("privateStaticExtension", "staticObject", "instanceObject", "instanceExtension")//
				.map(name -> dynamicTest(name, () -> assertThat(isStaticExtension).rejects(field(name))));
	}

	@Test
	void isInstanceExtension() {
		assertThat(isInstanceExtension).accepts(field("instanceExtension"));
	}

	@TestFactory
	Stream<DynamicTest> isNotInstanceExtension() {
		// @formatter:off
		return Stream.of("privateStaticExtension", "staticObject", "instanceObject", "privateInstanceExtension")
				.map(name -> dynamicTest(name, () -> assertThat(isInstanceExtension).rejects(field(name))));
		// @formatter:on
	}

	private Field field(String name) {
		try {
			return getClass().getDeclaredField(name);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static class DummyExtension implements Extension {
	}

}
