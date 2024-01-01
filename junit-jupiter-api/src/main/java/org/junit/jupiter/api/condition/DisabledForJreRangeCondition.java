/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.condition.EnabledOnJreCondition.DISABLED_ON_CURRENT_JRE;
import static org.junit.jupiter.api.condition.EnabledOnJreCondition.ENABLED_ON_CURRENT_JRE;

import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link DisabledForJreRange @DisabledForJreRange}.
 *
 * @since 5.6
 * @see DisabledForJreRange
 */
class DisabledForJreRangeCondition extends BooleanExecutionCondition<DisabledForJreRange> {

	DisabledForJreRangeCondition() {
		super(DisabledForJreRange.class, ENABLED_ON_CURRENT_JRE, DISABLED_ON_CURRENT_JRE,
			DisabledForJreRange::disabledReason);
	}

	@Override
	boolean isEnabled(DisabledForJreRange annotation) {
		JRE min = annotation.min();
		JRE max = annotation.max();

		Preconditions.condition((min != JRE.JAVA_8 || max != JRE.OTHER),
			"You must declare a non-default value for min or max in @DisabledForJreRange");
		Preconditions.condition(max.compareTo(min) >= 0,
			"@DisabledForJreRange.min must be less than or equal to @DisabledForJreRange.max");

		return !JRE.isCurrentVersionWithinRange(min, max);
	}

}
