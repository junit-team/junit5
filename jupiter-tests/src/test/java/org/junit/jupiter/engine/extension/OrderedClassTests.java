/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_CLASS_ORDER_PROPERTY_NAME;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

/**
 * Integration tests for {@link ClassOrderer} support.
 *
 * @since 5.8
 */
class OrderedClassTests {

	private static final List<String> callSequence = Collections.synchronizedList(new ArrayList<>());

	@BeforeEach
	@AfterEach
	void clearCallSequence() {
		callSequence.clear();
	}

	@Test
	void className() {
		executeTests(ClassOrderer.ClassName.class)//
				.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence)//
				.containsExactly("A_TestCase", "B_TestCase", "C_TestCase");
	}

	@Test
	void classNameAcrossPackages() {
		try {
			example.B_TestCase.callSequence = callSequence;

			// @formatter:off
			executeTests(ClassOrderer.ClassName.class, selectClass(B_TestCase.class), selectClass(example.B_TestCase.class))
					.assertStatistics(stats -> stats.succeeded(callSequence.size()));
			// @formatter:on

			assertThat(callSequence)//
					.containsExactly("example.B_TestCase", "B_TestCase");
		}
		finally {
			example.B_TestCase.callSequence = null;
		}
	}

	@Test
	void displayName() {
		executeTests(ClassOrderer.DisplayName.class)//
				.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence)//
				.containsExactly("C_TestCase", "B_TestCase", "A_TestCase");
	}

	@Test
	void orderAnnotation() {
		executeTests(ClassOrderer.OrderAnnotation.class)//
				.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence)//
				.containsExactly("A_TestCase", "C_TestCase", "B_TestCase");
	}

	@Test
	void orderAnnotationOnNestedTestClassesWithGlobalConfig() {
		executeTests(ClassOrderer.OrderAnnotation.class, selectClass(OuterWithGlobalConfig.class))//
				.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence)//
				.containsExactly("Inner2", "Inner1", "Inner0", "Inner3");
	}

	@Test
	void orderAnnotationOnNestedTestClassesWithLocalConfig(@TrackLogRecords LogRecordListener listener) {
		executeTests(ClassOrderer.class, selectClass(OuterWithLocalConfig.class))//
				.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		// Ensure that supplying the ClassOrderer interface instead of an implementation
		// class results in a WARNING log message. This also lets us know the local
		// config is used.
		assertTrue(listener.stream(Level.WARNING)//
				.map(LogRecord::getMessage)//
				.anyMatch(m -> m.startsWith(
					"Failed to load default class orderer class 'org.junit.jupiter.api.ClassOrderer'")));

		assertThat(callSequence)//
				.containsExactly("Inner2", "Inner1", "Inner1Inner1", "Inner1Inner0", "Inner0", "Inner3");
	}

	@Test
	void random() {
		executeTests(ClassOrderer.Random.class)//
				.assertStatistics(stats -> stats.succeeded(callSequence.size()));
	}

	private Events executeTests(Class<? extends ClassOrderer> classOrderer) {
		return executeTests(classOrderer, selectClass(A_TestCase.class), selectClass(B_TestCase.class),
			selectClass(C_TestCase.class));
	}

	private Events executeTests(Class<? extends ClassOrderer> classOrderer, DiscoverySelector... selectors) {
		// @formatter:off
		return EngineTestKit.engine("junit-jupiter")
			.configurationParameter(DEFAULT_TEST_CLASS_ORDER_PROPERTY_NAME, classOrderer.getName())
			.selectors(selectors)
			.execute()
			.testEvents();
		// @formatter:on
	}

	static abstract class BaseTestCase {

		@BeforeEach
		void trackInvocations(TestInfo testInfo) {
			var testClass = testInfo.getTestClass().get();

			callSequence.add(testClass.getSimpleName());
		}

		@Test
		void a() {
		}
	}

	@Order(2)
	@DisplayName("Z")
	static class A_TestCase extends BaseTestCase {
	}

	static class B_TestCase extends BaseTestCase {
	}

	@Order(10)
	@DisplayName("A")
	static class C_TestCase extends BaseTestCase {
	}

	static class OuterWithGlobalConfig {

		@Nested
		class Inner0 {
			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}
		}

		@Nested
		@Order(2)
		class Inner1 {
			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}
		}

		@Nested
		@Order(1)
		class Inner2 {
			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}
		}

		@Nested
		@Order(Integer.MAX_VALUE)
		class Inner3 {
			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}
		}
	}

	@TestClassOrder(ClassOrderer.OrderAnnotation.class)
	static class OuterWithLocalConfig {

		@Nested
		class Inner0 {
			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}
		}

		@Nested
		@Order(2)
		class Inner1 {

			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}

			@Nested
			@Order(2)
			class Inner1Inner0 {
				@Test
				void test() {
					callSequence.add(getClass().getSimpleName());
				}
			}

			@Nested
			@Order(1)
			class Inner1Inner1 {
				@Test
				void test() {
					callSequence.add(getClass().getSimpleName());
				}
			}
		}

		@Nested
		@Order(1)
		class Inner2 {
			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}
		}

		@Nested
		@Order(Integer.MAX_VALUE)
		class Inner3 {
			@Test
			void test() {
				callSequence.add(getClass().getSimpleName());
			}
		}
	}

}
