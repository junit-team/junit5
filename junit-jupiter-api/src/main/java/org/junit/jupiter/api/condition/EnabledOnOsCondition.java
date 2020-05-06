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

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link EnabledOnOs @EnabledOnOs}.
 *
 * @since 5.1
 * @see EnabledOnOs
 */
class EnabledOnOsCondition extends BooleanExecutionCondition<EnabledOnOs> {

	static final String ENABLED_ON_CURRENT_OS = "Enabled on operating system: " + System.getProperty("os.name");

	static final String DISABLED_ON_CURRENT_OS = "Disabled on operating system: " + System.getProperty("os.name");

	EnabledOnOsCondition() {
		super(EnabledOnOs.class, ENABLED_ON_CURRENT_OS, DISABLED_ON_CURRENT_OS, EnabledOnOs::disabledReason);
	}

	@Override
	boolean isEnabled(EnabledOnOs annotation) {
		OS[] operatingSystems = annotation.value();
		Preconditions.condition(operatingSystems.length > 0, "You must declare at least one OS in @EnabledOnOs");
		return Arrays.stream(operatingSystems).anyMatch(OS::isCurrentOs);
	}

}
