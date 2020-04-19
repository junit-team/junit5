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

import java.util.List;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.classes.AExecutionConditionClass;
import org.junit.platform.commons.util.classes.ATestExecutionListenerClass;
import org.junit.platform.commons.util.classes.AVanillaEmpty;
import org.junit.platform.commons.util.classes.BExecutionConditionClass;
import org.junit.platform.commons.util.classes.BTestExecutionListenerClass;
import org.junit.platform.commons.util.classes.BVanillaEmpty;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Unit tests for {@link ClassNamePatternFilterUtils}.
 *
 * @since 5.7
 */
@TestInstance(Lifecycle.PER_CLASS)
class ClassNamePatternFilterUtilsTests {

	//@formatter:off
	@ValueSource(strings = {
			"org.junit.jupiter.*",
			"org.junit.platform.*.NonExistentClass",
			"*.NonExistentClass*",
			"*NonExistentClass*",
			"AExecutionConditionClass, BExecutionConditionClass"
	})
	//@formatter:on
	@ParameterizedTest
	void alwaysEnabledConditions(String pattern) {
		List<? extends ExecutionCondition> executionConditions = asList(new AExecutionConditionClass(),
			new BExecutionConditionClass());
		assertThat(executionConditions).filteredOn(
			ClassNamePatternFilterUtils.excludeMatchingClasses(pattern)).isNotEmpty();
	}

	//@formatter:off
	@ValueSource(strings = {
			"org.junit.platform.*",
			"*.platform.*",
			"*",
			"*AExecutionConditionClass, *BExecutionConditionClass",
			"*ExecutionConditionClass"
	})
	//@formatter:on
	@ParameterizedTest
	void alwaysDisabledConditions(String pattern) {
		List<? extends ExecutionCondition> executionConditions = asList(new AExecutionConditionClass(),
			new BExecutionConditionClass());
		assertThat(executionConditions).filteredOn(
			ClassNamePatternFilterUtils.excludeMatchingClasses(pattern)).isEmpty();
	}

	//@formatter:off
	@ValueSource(strings = {
			"org.junit.jupiter.*",
			"org.junit.platform.*.NonExistentClass",
			"*.NonExistentClass*",
			"*NonExistentClass*",
			"ATestExecutionListenerClass, BTestExecutionListenerClass"
	})
	//@formatter:on
	@ParameterizedTest
	void alwaysEnabledListeners(String pattern) {
		List<? extends TestExecutionListener> executionConditions = asList(new ATestExecutionListenerClass(),
			new BTestExecutionListenerClass());
		assertThat(executionConditions).filteredOn(
			ClassNamePatternFilterUtils.excludeMatchingClasses(pattern)).isNotEmpty();
	}

	//@formatter:off
	@ValueSource(strings = {
			"org.junit.platform.*",
			"*.platform.*",
			"*",
			"*ATestExecutionListenerClass, *BTestExecutionListenerClass",
			"*TestExecutionListenerClass"
	})
	//@formatter:on
	@ParameterizedTest
	void alwaysDisabledListeners(String pattern) {
		List<? extends TestExecutionListener> executionConditions = asList(new ATestExecutionListenerClass(),
			new BTestExecutionListenerClass());
		assertThat(executionConditions).filteredOn(
			ClassNamePatternFilterUtils.excludeMatchingClasses(pattern)).isEmpty();
	}

	//@formatter:off
	@ValueSource(strings = {
			"org.junit.jupiter.*",
			"org.junit.platform.*.NonExistentClass",
			"*.NonExistentClass*",
			"*NonExistentClass*",
			"AVanillaEmpty, BVanillaEmpty"
	})
	//@formatter:on
	@ParameterizedTest
	void alwaysEnabledClass(String pattern) {
		List<Object> executionConditions = asList(new AVanillaEmpty(), new BVanillaEmpty());
		assertThat(executionConditions).filteredOn(
			ClassNamePatternFilterUtils.excludeMatchingClasses(pattern)).isNotEmpty();
	}

	//@formatter:off
	@ValueSource(strings = {
			"org.junit.platform.*",
			"*.platform.*",
			"*",
			"*AVanillaEmpty, *BVanillaEmpty",
			"*VanillaEmpty"
	})
	//@formatter:on
	@ParameterizedTest
	void alwaysDisabledClass(String pattern) {
		List<Object> executionConditions = asList(new AVanillaEmpty(), new BVanillaEmpty());
		assertThat(executionConditions).filteredOn(
			ClassNamePatternFilterUtils.excludeMatchingClasses(pattern)).isEmpty();
	}
}
