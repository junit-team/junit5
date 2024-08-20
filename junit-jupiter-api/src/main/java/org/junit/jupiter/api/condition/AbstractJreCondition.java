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

import java.lang.annotation.Annotation;
import java.util.function.Function;

import org.junit.platform.commons.util.Preconditions;

/**
 * Abstract base class for {@link EnabledOnJreCondition} and
 * {@link DisabledOnJreCondition}.
 *
 * @since 5.12
 */
abstract class AbstractJreCondition<A extends Annotation> extends BooleanExecutionCondition<A> {

	static final String ENABLED_ON_CURRENT_JRE = //
		"Enabled on JRE version: " + System.getProperty("java.version");

	static final String DISABLED_ON_CURRENT_JRE = //
		"Disabled on JRE version: " + System.getProperty("java.version");

	private final String annotationName;

	AbstractJreCondition(Class<A> annotationType, Function<A, String> customDisabledReason) {
		super(annotationType, ENABLED_ON_CURRENT_JRE, DISABLED_ON_CURRENT_JRE, customDisabledReason);
		this.annotationName = annotationType.getSimpleName();
	}

	protected void validateVersions(JRE[] jres, int[] versions) {
		Preconditions.condition(jres.length > 0 || versions.length > 0,
			() -> "You must declare at least one JRE or version in @" + this.annotationName);
		for (JRE jre : jres) {
			Preconditions.condition(jre != JRE.UNDEFINED,
				() -> "JRE.UNDEFINED is not supported in @" + this.annotationName);
		}
		for (int version : versions) {
			Preconditions.condition(version >= 8,
				() -> String.format("Version [%d] in @%s must be greater than or equal to 8", version,
					this.annotationName));
		}
	}

}
