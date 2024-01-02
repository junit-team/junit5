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
 * {@link ExecutionCondition} for {@link EnabledForJreRange @EnabledForJreRange}.
 *
 * @since 5.6
 * @see EnabledForJreRange
 */
class EnabledForJreRangeCondition extends BooleanExecutionCondition<EnabledForJreRange> {

	EnabledForJreRangeCondition() {
		super(EnabledForJreRange.class, ENABLED_ON_CURRENT_JRE, DISABLED_ON_CURRENT_JRE,
			EnabledForJreRange::disabledReason);
	}

	@Override
	boolean isEnabled(EnabledForJreRange annotation) {
		JRE min = annotation.min();
		JRE max = annotation.max();

		Preconditions.condition((min != JRE.JAVA_8 || max != JRE.OTHER),
			"You must declare a non-default value for min or max in @EnabledForJreRange");
		Preconditions.condition(max.compareTo(min) >= 0,
			"@EnabledForJreRange.min must be less than or equal to @EnabledForJreRange.max");

		return JRE.isCurrentVersionWithinRange(min, max);
	}

}
