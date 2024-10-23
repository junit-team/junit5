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
		JRE minJre = annotation.min();
		JRE maxJre = annotation.max();
		int minFeatureVersion = annotation.minFeatureVersion();
		int maxFeatureVersion = annotation.maxFeatureVersion();

		Preconditions.condition(!(minJre != JRE.JAVA_8 && minFeatureVersion != -1),
			"@EnabledForJreRange's minimum value must be configured with either a JRE or feature version, but not both");
		Preconditions.condition(!(maxJre != JRE.OTHER && maxFeatureVersion != -1),
			"@EnabledForJreRange's maximum value must be configured with either a JRE or feature version, but not both");

		boolean minValueConfigured = minJre != JRE.JAVA_8 || minFeatureVersion != -1;
		boolean maxValueConfigured = maxJre != JRE.OTHER || maxFeatureVersion != -1;
		Preconditions.condition(minValueConfigured || maxValueConfigured,
			"You must declare a non-default value for the minimum or maximum value in @EnabledForJreRange");

		int min = (minFeatureVersion != -1 ? minFeatureVersion : minJre.featureVersion());
		int max = (maxFeatureVersion != -1 ? maxFeatureVersion : maxJre.featureVersion());
		Preconditions.condition(min <= max,
			"@EnabledForJreRange's minimum value must be less than or equal to its maximum value");

		return JRE.isCurrentVersionWithinRange(min, max);
	}

}
