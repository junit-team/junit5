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

import static org.junit.jupiter.api.condition.AbstractJreCondition.DISABLED_ON_CURRENT_JRE;
import static org.junit.jupiter.api.condition.AbstractJreCondition.ENABLED_ON_CURRENT_JRE;

import org.junit.jupiter.api.extension.ExecutionCondition;

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
		return !EnabledForJreRangeCondition.isCurrentVersionWithinRange("DisabledForJreRange", annotation.min(),
			annotation.max(), annotation.minVersion(), annotation.maxVersion());
	}

}
