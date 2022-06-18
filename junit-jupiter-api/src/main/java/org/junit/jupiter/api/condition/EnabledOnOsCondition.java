/*
 * Copyright 2015-2022 the original author or authors.
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

	static final String ENABLED_ON_CURRENT_OS = String.format("Enabled on operating system: %s (%s)",
		System.getProperty("os.name"), System.getProperty("os.arch"));

	static final String DISABLED_ON_CURRENT_OS = String.format("Disabled on operating system: %s (%s)",
		System.getProperty("os.name"), System.getProperty("os.arch"));

	EnabledOnOsCondition() {
		super(EnabledOnOs.class, ENABLED_ON_CURRENT_OS, DISABLED_ON_CURRENT_OS, EnabledOnOs::disabledReason);
	}

	@Override
	boolean isEnabled(EnabledOnOs annotation) {
		Preconditions.condition(annotation.value().length > 0 || annotation.architectures().length > 0,
			"You must declare at least one OS or architecture in @DisabledOnOs");
		return isEnabledBasedOnOs(annotation) || isEnabledBasedOnArchitecture(annotation);
	}

	private boolean isEnabledBasedOnArchitecture(EnabledOnOs annotation) {
		String[] architectures = annotation.architectures();
		String arch = getArchitecture();
		return Arrays.stream(architectures).anyMatch(arch::equalsIgnoreCase);
	}

	private boolean isEnabledBasedOnOs(EnabledOnOs annotation) {
		OS[] operatingSystems = annotation.value();
		return Arrays.stream(operatingSystems).anyMatch(OS::isCurrentOs);
	}

	protected String getArchitecture() {
		return System.getProperty("os.arch");
	}

}
