/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.Extension;

/**
 * Unit tests for {@link ExtensionUtils}.
 *
 * @since 5.1
 */
class ExtensionUtilsTests {

	private static final Predicate<Field> isPotentialStaticExtension = ExtensionUtils.isNonPrivateStaticField;
	private static final Predicate<Field> isPotentialInstanceExtension = ExtensionUtils.isNonPrivateInstanceField;

	static Extension staticExtension = new DummyExtension();
	Extension instanceExtension = new DummyExtension();

	static Object staticObject = new DummyExtension();
	Object instanceObject = new DummyExtension();

	@SuppressWarnings("unused")
	private static Extension privateStaticExtension = new DummyExtension();

	@SuppressWarnings("unused")
	private Extension privateInstanceExtension = new DummyExtension();

	@TestFactory
	Stream<DynamicTest> isPotentialStaticExtension() {
		return Stream.of("staticExtension", "staticObject")//
				.map(name -> dynamicTest(name, () -> assertThat(isPotentialStaticExtension).accepts(field(name))));
	}

	@TestFactory
	Stream<DynamicTest> isNotStaticExtension() {
		return Stream.of("privateStaticExtension", "instanceObject", "instanceExtension")//
				.map(name -> dynamicTest(name, () -> assertThat(isPotentialStaticExtension).rejects(field(name))));
	}

	@TestFactory
	Stream<DynamicTest> isPotentialInstanceExtension() {
		return Stream.of("instanceExtension", "instanceObject")//
				.map(name -> dynamicTest(name, () -> assertThat(isPotentialInstanceExtension).accepts(field(name))));
	}

	@TestFactory
	Stream<DynamicTest> isNotInstanceExtension() {
		return Stream.of("privateStaticExtension", "staticObject", "privateInstanceExtension")//
				.map(name -> dynamicTest(name, () -> assertThat(isPotentialInstanceExtension).rejects(field(name))));
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
