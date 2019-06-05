/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;

/**
 * Integration tests that verify support for {@linkplain Order ordered} programmatic
 * extension registration via {@link RegisterExtension @RegisterExtension} in the
 * {@link JupiterTestEngine}.
 *
 * @since 5.4
 * @see ProgrammaticExtensionRegistrationTests
 */
class OrderedProgrammaticExtensionRegistrationTests extends AbstractJupiterTestEngineTests {

	private static final List<Integer> callSequence = new ArrayList<>();

	/**
	 * This method basically verifies the implementation of
	 * {@link java.lang.String#hashCode()} (which needn't really be tested)
	 * in order to make reasonable assumptions about how fields are sorted
	 * in {@link org.junit.platform.commons.util.ReflectionUtils#defaultFieldSorter(Field, Field)}.
	 *
	 * <p>In other words, this method is just a sanity check for the chosen
	 * field names in the test cases used in these tests.
	 */
	@BeforeAll
	static void assertAssumptionsAboutDefaultOrderingAlgorithm() {
		String fieldName1 = "extension1";
		String fieldName2 = "extension2";
		String fieldName3 = "extension3";

		assertThat(fieldName1.hashCode()).isLessThan(fieldName2.hashCode());
		assertThat(fieldName2.hashCode()).isLessThan(fieldName3.hashCode());
	}

	@BeforeEach
	void clearCallSequence() {
		callSequence.clear();
	}

	@Test
	void instanceLevelWithDefaultOrder() {
		assertOutcome(DefaultOrderInstanceLevelExtensionRegistrationTestCase.class, 1, 2, 3);
	}

	@Test
	void instanceLevelWithExplicitOrder() {
		assertOutcome(ExplicitOrderInstanceLevelExtensionRegistrationTestCase.class, 3, 2, 1);
	}

	@Test
	void instanceLevelWithDefaultOrderAndExplicitOrder() {
		assertOutcome(DefaultOrderAndExplicitOrderInstanceLevelExtensionRegistrationTestCase.class, 3, 1, 2);
	}

	@Test
	void instanceLevelWithDefaultOrderAndExplicitOrderWithTestInstancePerClassLifecycle() {
		assertOutcome(
			DefaultOrderAndExplicitOrderInstanceLevelExtensionRegistrationWithTestInstancePerClassLifecycleTestCase.class,
			3, 1, 2);
	}

	@Test
	void classLevelWithDefaultOrderAndExplicitOrder() {
		assertOutcome(DefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase.class, 3, 1, 2);
	}

	@Test
	void classLevelWithDefaultOrderAndExplicitOrderInheritedFromSuperclass() {
		assertOutcome(InheritedDefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase.class, 3, 1, 2);
	}

	@Test
	void classLevelWithDefaultOrderShadowingOrderFromSuperclass() {
		assertOutcome(DefaultOrderShadowingDefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase.class, 1,
			2, 3);
	}

	@Test
	void classLevelWithExplicitOrderShadowingOrderFromSuperclass() {
		assertOutcome(ExplicitOrderShadowingDefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase.class,
			3, 2, 1);
	}

	@Test
	void classLevelWithDefaultOrderAndExplicitOrderFromInterface() {
		assertOutcome(DefaultOrderAndExplicitOrderExtensionRegistrationFromInterfaceTestCase.class, 3, 1, 2);
	}

	private void assertOutcome(Class<?> testClass, Integer... values) {
		executeTestsForClass(testClass).tests().assertStatistics(stats -> stats.succeeded(1));
		assertThat(callSequence).containsExactly(values);
	}

	// -------------------------------------------------------------------

	private static class AbstractTestCase {

		@Test
		void test() {
		}

	}

	static class DefaultOrderInstanceLevelExtensionRegistrationTestCase extends AbstractTestCase {

		@RegisterExtension
		Extension extension1 = new BeforeEachExtension(1);

		@RegisterExtension
		Extension extension3 = new BeforeEachExtension(3);

		@RegisterExtension
		Extension extension2 = new BeforeEachExtension(2);

	}

	static class ExplicitOrderInstanceLevelExtensionRegistrationTestCase extends AbstractTestCase {

		@Order(3)
		@RegisterExtension
		Extension extension1 = new BeforeEachExtension(1);

		@Order(2)
		@RegisterExtension
		Extension extension2 = new BeforeEachExtension(2);

		@Order(1)
		@RegisterExtension
		Extension extension3 = new BeforeEachExtension(3);

	}

	static class DefaultOrderAndExplicitOrderInstanceLevelExtensionRegistrationTestCase extends AbstractTestCase {

		// @Order(3)
		@RegisterExtension
		Extension extension1 = new BeforeEachExtension(1);

		// @Order(2)
		@RegisterExtension
		Extension extension2 = new BeforeEachExtension(2);

		@Order(1)
		@RegisterExtension
		Extension extension3 = new BeforeEachExtension(3);

	}

	@TestInstance(PER_CLASS)
	static class DefaultOrderAndExplicitOrderInstanceLevelExtensionRegistrationWithTestInstancePerClassLifecycleTestCase
			extends AbstractTestCase {

		// @Order(3)
		@RegisterExtension
		Extension extension1 = new BeforeEachExtension(1);

		// @Order(2)
		@RegisterExtension
		Extension extension2 = new BeforeEachExtension(2);

		@Order(1)
		@RegisterExtension
		Extension extension3 = new BeforeEachExtension(3);

	}

	static class DefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase extends AbstractTestCase {

		// @Order(3)
		@RegisterExtension
		static Extension extension1 = new BeforeEachExtension(1);

		// @Order(2)
		@RegisterExtension
		static Extension extension2 = new BeforeEachExtension(2);

		@Order(1)
		@RegisterExtension
		static Extension extension3 = new BeforeEachExtension(3);

	}

	static class InheritedDefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase
			extends DefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase {
	}

	static class DefaultOrderShadowingDefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase
			extends DefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase {

		// @Order(1)
		@RegisterExtension
		static Extension extension3 = new BeforeEachExtension(3);

	}

	static class ExplicitOrderShadowingDefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase
			extends DefaultOrderAndExplicitOrderClassLevelExtensionRegistrationTestCase {

		@Order(2)
		@RegisterExtension
		static Extension extension2 = new BeforeEachExtension(2);

	}

	interface DefaultOrderAndExplicitOrderClassLevelExtensionRegistrationInterface {

		// @Order(3)
		@RegisterExtension
		static Extension extension1 = new BeforeEachExtension(1);

		// @Order(2)
		@RegisterExtension
		static Extension extension2 = new BeforeEachExtension(2);

		@Order(1)
		@RegisterExtension
		static Extension extension3 = new BeforeEachExtension(3);

	}

	static class DefaultOrderAndExplicitOrderExtensionRegistrationFromInterfaceTestCase extends AbstractTestCase
			implements DefaultOrderAndExplicitOrderClassLevelExtensionRegistrationInterface {
	}

	private static class BeforeEachExtension implements BeforeEachCallback {

		private final int id;

		BeforeEachExtension(int id) {
			this.id = id;
		}

		@Override
		public void beforeEach(ExtensionContext context) {
			callSequence.add(this.id);
		}

	}

}
