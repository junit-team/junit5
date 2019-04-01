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

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.*;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;

/**
 * Unit tests for {@link DisplayNameUtils}.
 *
 * @since 5.0
 */
class DisplayNameUtilsTests {

	@Nested
	class ClassDisplayNameTests {
		@Test
		void shouldGetDisplayNameFromDisplayNameAnnotation() {

			String displayName = DisplayNameUtils.determineDisplayName(MyTestCase.class, () -> "default-name");

			assertThat(displayName).isEqualTo("my-test-case");
		}

		@Test
		@TrackLogRecords
		void shouldGetDisplayNameFromSupplierIfNoDisplayNameAnnotationWithBlankStringPresent(
				LogRecordListener listener) {

			String displayName = DisplayNameUtils.determineDisplayName(BlankDisplayNameTestCase.class,
				() -> "default-name");

			assertThat(displayName).isEqualTo("default-name");
			assertThat(firstWarningLogRecord(listener).getMessage()).isEqualTo(
				"Configuration error: @DisplayName on [class org.junit.jupiter.engine.descriptor.DisplayNameUtilsTests$BlankDisplayNameTestCase] must be declared with a non-empty value.");
		}

		@Test
		void shouldGetDisplayNameFromSupplierIfNoDisplayNameAnnotationPresent() {

			String displayName = DisplayNameUtils.determineDisplayName(NotDisplayNameTestCase.class,
				() -> "default-name");

			assertThat(displayName).isEqualTo("default-name");
		}

		@Nested
		class ClassDisplayNameSupplierTests {

			@Test
			void shouldGetDisplayNameFromJupiterConfiguration() {
				Supplier<String> displayName = DisplayNameUtils.createDisplayNameSupplierForClass(MyTestCase.class,
					Optional.of(CustomDisplayNameGenerator.class));

				assertThat(displayName.get()).isEqualTo("class-display-name");
			}

			@Test
			void shouldGetDisplayNameFromDisplayNameGenerationAnnotationWhenNoConfigurationPresent() {
				Supplier<String> displayName = DisplayNameUtils.createDisplayNameSupplierForClass(MyTestCase.class,
					Optional.empty());

				assertThat(displayName.get()).isEqualTo("class-display-name");
			}

			@Test
			void shouldGetStandardDisplayNameIfAnnotationAndNoConfigurationPresent() {
				Supplier<String> displayName = DisplayNameUtils.createDisplayNameSupplierForClass(
					NotDisplayNameTestCase.class, Optional.empty());

				String name = NotDisplayNameTestCase.class.getName();
				String expectedClassName = name.substring(name.lastIndexOf(".") + 1);
				assertThat(displayName.get()).isEqualTo(expectedClassName);
			}

			@Test
			void shouldGetStandardDisplayNameFromDisplayNameGenerationAnnotation() {
				Supplier<String> displayName = DisplayNameUtils.createDisplayNameSupplierForClass(
					StandardDisplayNameTestCase.class, Optional.empty());

				String name = StandardDisplayNameTestCase.class.getName();
				String expectedClassName = name.substring(name.lastIndexOf(".") + 1);
				assertThat(displayName.get()).isEqualTo(expectedClassName);
			}

			@Test
			void shouldGetUnderscoreDisplayNameFromDisplayNameGenerationAnnotation() {
				Supplier<String> displayName = DisplayNameUtils.createDisplayNameSupplierForClass(
					Underscore_DisplayName_TestCase.class, Optional.empty());

				assertThat(displayName.get()).isEqualTo("DisplayNameUtilsTests$Underscore DisplayName TestCase");
			}
		}
	}

	@Nested
	class NestedClassDisplayNameTests {
		@Test
		void shouldGetDisplayNameFromConfigurationClass() {
			Supplier<String> displayName = DisplayNameUtils.createDisplayNameSupplierForNestedClass(
				NestedTestCase.class, Optional.of(CustomDisplayNameGenerator.class));

			assertThat(displayName.get()).isEqualTo("nested-class-display-name");
		}

		@Test
		void shouldGetDisplayNameFromStandardIfConfigurationClass() {
			Supplier<String> displayName = DisplayNameUtils.createDisplayNameSupplierForNestedClass(
				NestedTestCase.class, Optional.empty());

			assertThat(displayName.get()).isEqualTo("NestedTestCase");
		}
	}

	@Nested
	class MethodDisplayNameTests {
		@Test
		void shouldGetDisplayNameFromConfigurationClass() throws Exception {
			Method method = MyTestCase.class.getDeclaredMethod("test1");

			String displayName = DisplayNameUtils.determineDisplayNameForMethod(NestedTestCase.class, method,
				Optional.of(CustomDisplayNameGenerator.class));

			assertThat(displayName).isEqualTo("method-display-name");
		}

		@Test
		void shouldGetDisplayNameFromStandardIfConfigurationClass() throws Exception {
			Method method = MyTestCase.class.getDeclaredMethod("test1");

			String displayName = DisplayNameUtils.determineDisplayNameForMethod(NestedTestCase.class, method,
				Optional.empty());

			assertThat(displayName).isEqualTo("test1()");
		}
	}

	private LogRecord firstWarningLogRecord(LogRecordListener listener) throws AssertionError {
		return listener.stream(DisplayNameUtils.class, Level.WARNING).findFirst().orElseThrow(
			() -> new AssertionError("Failed to find warning log record"));
	}

	@DisplayName("my-test-case")
	@DisplayNameGeneration(value = CustomDisplayNameGenerator.class)
	static class MyTestCase {

		void test1() {
		}

	}

	@DisplayName("")
	static class BlankDisplayNameTestCase {
	}

	@DisplayNameGeneration(value = DisplayNameGenerator.Standard.class)
	static class StandardDisplayNameTestCase {
	}

	@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
	static class Underscore_DisplayName_TestCase {
	}

	static class NotDisplayNameTestCase {
	}

	@Nested
	class NestedTestCase {
	}

}
