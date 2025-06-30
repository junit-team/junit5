/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link ConditionEvaluationResult}.
 *
 * @since 5.13.3
 */
class ConditionEvaluationResultTests {

	@Test
	void enabledWithReason() {
		var result = ConditionEvaluationResult.enabled("reason");

		assertThat(result.isDisabled()).isFalse();
		assertThat(result.getReason()).contains("reason");
		assertThat(result).asString()//
				.isEqualTo("ConditionEvaluationResult [enabled = true, reason = 'reason']");
	}

	@EmptyReasonsTest
	void enabledWithInvalidReason(@Nullable String reason) {
		@SuppressWarnings("NullAway")
		var result = ConditionEvaluationResult.enabled(reason);

		assertThat(result.isDisabled()).isFalse();

		if (reason == null) {
			assertThat(result.getReason()).isEmpty();
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = true, reason = '<unknown>']");
		}
		// TODO Remove else-block once issues are addressed.
		else {
			assertThat(result.getReason()).contains(reason);
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = true, reason = '%s']", reason);
		}
	}

	@Test
	void disabledWithDefaultReason() {
		var result = ConditionEvaluationResult.disabled("default");

		assertThat(result.isDisabled()).isTrue();
		assertThat(result.getReason()).contains("default");
		assertThat(result).asString()//
				.isEqualTo("ConditionEvaluationResult [enabled = false, reason = 'default']");
	}

	@EmptyReasonsTest
	void disabledWithInvalidDefaultReason(@Nullable String reason) {
		@SuppressWarnings("NullAway")
		var result = ConditionEvaluationResult.disabled(reason);

		assertThat(result.isDisabled()).isTrue();

		if (reason == null) {
			assertThat(result.getReason()).isEmpty();
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = false, reason = '<unknown>']");
		}
		// TODO Remove else-block once issues are addressed.
		else {
			assertThat(result.getReason()).contains(reason);
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = false, reason = '%s']", reason);
		}
	}

	@EmptyReasonsTest
	void disabledWithValidDefaultReasonAndInvalidCustomReason(@Nullable String customReason) {
		@SuppressWarnings("NullAway")
		var result = ConditionEvaluationResult.disabled("default", customReason);

		assertThat(result.isDisabled()).isTrue();
		assertThat(result.getReason()).contains("default");
		assertThat(result).asString()//
				.isEqualTo("ConditionEvaluationResult [enabled = false, reason = 'default']");
	}

	@EmptyReasonsTest
	void disabledWithInvalidDefaultReasonAndValidCustomReason(@Nullable String reason) {
		@SuppressWarnings("NullAway")
		var result = ConditionEvaluationResult.disabled(reason, "custom");

		assertThat(result.isDisabled()).isTrue();

		// TODO Convert to single assertion once issues are addressed.
		// The following should hold for all null/blank default reasons.
		// assertThat(result).asString().isEqualTo("ConditionEvaluationResult [enabled = false, reason = 'custom']");

		if (reason == null) {
			assertThat(result.getReason()).contains("null ==> custom");
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = false, reason = 'null ==> custom']");
		}
		else {
			var generatedReason = reason + " ==> custom";
			assertThat(result.getReason()).contains(generatedReason);
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = false, reason = '%s']", generatedReason);
		}
	}

	@EmptyReasonsTest
	void disabledWithInvalidDefaultReasonAndInvalidCustomReason(@Nullable String reason) {
		// We intentionally use the reason as both the default and custom reason.
		@SuppressWarnings("NullAway")
		var result = ConditionEvaluationResult.disabled(reason, reason);

		assertThat(result.isDisabled()).isTrue();

		if (reason == null) {
			assertThat(result.getReason()).isEmpty();
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = false, reason = '<unknown>']");
		}
		// TODO Remove else-block once issues are addressed.
		else {
			assertThat(result.getReason()).contains(reason);
			assertThat(result).asString()//
					.isEqualTo("ConditionEvaluationResult [enabled = false, reason = '%s']", reason);
		}
	}

	@Test
	void disabledWithValidDefaultReasonAndCustomReason() {
		var result = ConditionEvaluationResult.disabled("default", "custom");

		assertThat(result.isDisabled()).isTrue();
		assertThat(result.getReason()).contains("default ==> custom");
		assertThat(result).asString()//
				.isEqualTo("ConditionEvaluationResult [enabled = false, reason = 'default ==> custom']");
	}

	@Retention(RetentionPolicy.RUNTIME)
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", " ", "   ", "\t", "\n" })
	@interface EmptyReasonsTest {
	}

}
