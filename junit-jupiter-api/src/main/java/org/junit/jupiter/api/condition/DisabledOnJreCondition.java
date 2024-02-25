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

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link DisabledOnJre @DisabledOnJre}.
 *
 * @since 5.1
 * @see DisabledOnJre
 */
class DisabledOnJreCondition extends BooleanExecutionCondition<DisabledOnJre> {

	DisabledOnJreCondition() {
		super(DisabledOnJre.class, ENABLED_ON_CURRENT_JRE, DISABLED_ON_CURRENT_JRE, DisabledOnJre::disabledReason);
	}

	@Override
	boolean isEnabled(DisabledOnJre annotation) {
		JRE[] versions = annotation.value();
		Preconditions.condition(versions.length > 0, "You must declare at least one JRE in @DisabledOnJre");
		return Arrays.stream(versions).noneMatch(JRE::isCurrentVersion);
	}

}
