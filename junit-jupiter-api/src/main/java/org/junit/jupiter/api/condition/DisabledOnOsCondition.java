/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.condition.EnabledOnOsCondition.DISABLED_ON_CURRENT_OS;
import static org.junit.jupiter.api.condition.EnabledOnOsCondition.ENABLED_ON_CURRENT_OS;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.Arrays;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link DisabledOnOs @DisabledOnOs}.
 *
 * @see DisabledOnOs
 * @since 5.1
 */
class DisabledOnOsCondition extends BooleanExecutionCondition<DisabledOnOs> {

	DisabledOnOsCondition() {
		super(DisabledOnOs.class);
	}

	@Override
	ConditionEvaluationResult defaultResult() {
		return enabled("@DisabledOnOs is not present");
	}

	@Override
	String disabledReason(DisabledOnOs annotation) {
		String customReason = annotation.disabledReason();
		if (customReason.isEmpty()) {
			return DISABLED_ON_CURRENT_OS;
		}
		return String.format("%s ==> %s", DISABLED_ON_CURRENT_OS, customReason);

	}

	@Override
	String enabledReason(DisabledOnOs annotation) {
		return ENABLED_ON_CURRENT_OS;
	}

	@Override
	boolean isEnabled(DisabledOnOs annotation) {
		OS[] operatingSystems = annotation.value();
		Preconditions.condition(operatingSystems.length > 0, "You must declare at least one OS in @DisabledOnOs");
		return Arrays.stream(operatingSystems).noneMatch(OS::isCurrentOs);
	}

}
