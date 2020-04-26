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

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link DisabledOnOs @DisabledOnOs}.
 *
 * @since 5.1
 * @see DisabledOnOs
 */
class DisabledOnOsCondition extends BooleanExecutionCondition<DisabledOnOs> {

	DisabledOnOsCondition() {
		super(DisabledOnOs.class, ENABLED_ON_CURRENT_OS, DISABLED_ON_CURRENT_OS, DisabledOnOs::disabledReason);
	}

	@Override
	boolean isEnabled(DisabledOnOs annotation) {
		OS[] operatingSystems = annotation.value();
		Preconditions.condition(operatingSystems.length > 0, "You must declare at least one OS in @DisabledOnOs");
		return Arrays.stream(operatingSystems).noneMatch(OS::isCurrentOs);
	}

}
