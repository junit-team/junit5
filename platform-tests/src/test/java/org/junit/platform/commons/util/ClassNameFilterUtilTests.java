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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.classes.AExecutionConditionClass;
import org.junit.platform.commons.util.classes.ATestExecutionListenerClass;
import org.junit.platform.commons.util.classes.AVanillaEmpty;
import org.junit.platform.commons.util.classes.BExecutionConditionClass;
import org.junit.platform.commons.util.classes.BTestExecutionListenerClass;
import org.junit.platform.commons.util.classes.BVanillaEmpty;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Unit tests for {@link ClassNameFilterUtil}.
 *
 * @since 5.7
 */
@SuppressWarnings({ "unchecked", "unused" })
@TestInstance(Lifecycle.PER_CLASS)
class ClassNameFilterUtilTests {

	Stream<Arguments> alwaysEnabledConditions() {
		return Stream.of(arguments("org.junit.jupiter.*"), arguments("org.junit.platform.*.NonExistentClass"),
			arguments("*.NonExistentClass*"), arguments("*NonExistentClass*"),
			arguments("AExecutionConditionClass, BExecutionConditionClass"));
	}

	@MethodSource
	@ParameterizedTest
	void alwaysEnabledConditions(String pattern) {
		//@formatter:off
		List<? extends ExecutionCondition> executionConditions = asList(new AExecutionConditionClass(), new BExecutionConditionClass());
		assertThat(executionConditions)
				.filteredOn(
						(Predicate<ExecutionCondition>)
				ClassNameFilterUtil.filterForClassName(pattern))
				.isNotEmpty();
		//@formatter:on
	}

	Stream<Arguments> alwaysDisabledConditions() {
		return Stream.of(arguments("org.junit.platform.*"), arguments("*.platform.*"), arguments("*"),
			arguments("*AExecutionConditionClass, *BExecutionConditionClass"), arguments("*ExecutionConditionClass"));
	}

	@MethodSource
	@ParameterizedTest
	void alwaysDisabledConditions(String pattern) {
		//@formatter:off
		List<? extends ExecutionCondition> executionConditions = asList(new AExecutionConditionClass(), new BExecutionConditionClass());
		assertThat(executionConditions)
				.filteredOn(
						(Predicate<ExecutionCondition>)
								ClassNameFilterUtil.filterForClassName(pattern))
				.isEmpty();
		//@formatter:on
	}

	Stream<Arguments> alwaysEnabledListeners() {
		return Stream.of(arguments("org.junit.jupiter.*"), arguments("org.junit.platform.*.NonExistentClass"),
			arguments("*.NonExistentClass*"), arguments("*NonExistentClass*"),
			arguments("ATestExecutionListenerClass, BTestExecutionListenerClass"));
	}

	@MethodSource
	@ParameterizedTest
	void alwaysEnabledListeners(String pattern) {
		//@formatter:off
		List<? extends TestExecutionListener> executionConditions = asList(new ATestExecutionListenerClass(), new BTestExecutionListenerClass());
		assertThat(executionConditions)
				.filteredOn(
						(Predicate<TestExecutionListener>)
								ClassNameFilterUtil.filterForClassName(pattern))
				.isNotEmpty();
		//@formatter:on
	}

	Stream<Arguments> alwaysDisabledListeners() {
		return Stream.of(arguments("org.junit.platform.*"), arguments("*.platform.*"), arguments("*"),
			arguments("*ATestExecutionListenerClass, *BTestExecutionListenerClass"),
			arguments("*TestExecutionListenerClass"));
	}

	@MethodSource
	@ParameterizedTest
	void alwaysDisabledListeners(String pattern) {
		//@formatter:off
		List<? extends TestExecutionListener> executionConditions = asList(new ATestExecutionListenerClass(), new BTestExecutionListenerClass());
		assertThat(executionConditions)
				.filteredOn(
						(Predicate<TestExecutionListener>)
								ClassNameFilterUtil.filterForClassName(pattern))
				.isEmpty();
		//@formatter:on
	}

	Stream<Arguments> alwaysEnabledClass() {
		return Stream.of(arguments("org.junit.jupiter.*"), arguments("org.junit.platform.*.NonExistentClass"),
			arguments("*.NonExistentClass*"), arguments("*NonExistentClass*"),
			arguments("AVanillaEmpty, BVanillaEmpty"));
	}

	@MethodSource
	@ParameterizedTest
	void alwaysEnabledClass(String pattern) {
		//@formatter:off
		List<Object> executionConditions = asList(new AVanillaEmpty(), new BVanillaEmpty());
		assertThat(executionConditions)
				.filteredOn(
						(Predicate<Object>)
								ClassNameFilterUtil.filterForClassName(pattern))
				.isNotEmpty();
		//@formatter:on
	}

	Stream<Arguments> alwaysDisabledClass() {
		return Stream.of(arguments("org.junit.platform.*"), arguments("*.platform.*"), arguments("*"),
			arguments("*AVanillaEmpty, *BVanillaEmpty"), arguments("*VanillaEmpty"));
	}

	@MethodSource
	@ParameterizedTest
	void alwaysDisabledClass(String pattern) {
		//@formatter:off
		List<Object> executionConditions = asList(new AVanillaEmpty(), new BVanillaEmpty());
		assertThat(executionConditions)
				.filteredOn(
						(Predicate<Object>)
								ClassNameFilterUtil.filterForClassName(pattern))
				.isEmpty();
		//@formatter:on
	}

}
