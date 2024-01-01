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

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link EnabledOnJre @EnabledOnJre}.
 *
 * @since 5.1
 * @see EnabledOnJre
 */
class EnabledOnJreCondition extends BooleanExecutionCondition<EnabledOnJre> {

	static final String ENABLED_ON_CURRENT_JRE = //
		"Enabled on JRE version: " + System.getProperty("java.version");

	static final String DISABLED_ON_CURRENT_JRE = //
		"Disabled on JRE version: " + System.getProperty("java.version");

	EnabledOnJreCondition() {
		super(EnabledOnJre.class, ENABLED_ON_CURRENT_JRE, DISABLED_ON_CURRENT_JRE, EnabledOnJre::disabledReason);
	}

	@Override
	boolean isEnabled(EnabledOnJre annotation) {
		JRE[] versions = annotation.value();
		Preconditions.condition(versions.length > 0, "You must declare at least one JRE in @EnabledOnJre");
		return Arrays.stream(versions).anyMatch(JRE::isCurrentVersion);
	}

}
