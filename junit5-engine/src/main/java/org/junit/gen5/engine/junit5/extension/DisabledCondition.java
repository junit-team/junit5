/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;

import java.util.Optional;

import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.api.extension.TestLifecycleExtension;
import org.junit.gen5.commons.util.StringUtils;

/**
 * @since 5.0
 * @see Disabled
 */
public class DisabledCondition implements TestLifecycleExtension {

	@Override
	public ConditionEvaluationResult shouldTestBeExecuted(TestExtensionContext context) {
		// Class level?
		Optional<Disabled> disabled = findAnnotation(context.getTestClass(), Disabled.class);

		// Method level?
		if (!disabled.isPresent()) {
			disabled = findAnnotation(context.getTestMethod(), Disabled.class);
		}

		if (disabled.isPresent()) {
			String reason = disabled.map(Disabled::value).filter(StringUtils::isNotBlank).orElse("Test is @Disabled");
			return ConditionEvaluationResult.disabled(reason);
		}

		return ConditionEvaluationResult.enabled("@Disabled is not present");
	}
}
