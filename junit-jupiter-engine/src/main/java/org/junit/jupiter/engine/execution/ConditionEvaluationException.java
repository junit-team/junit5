/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.platform.commons.JUnitException;

/**
 * Thrown if an error is encountered while evaluating a {@link ContainerExecutionCondition}
 * or {@link TestExecutionCondition}.
 *
 * @since 5.0
 * @see ConditionEvaluator
 */
class ConditionEvaluationException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ConditionEvaluationException(String message, Throwable cause) {
		super(message, cause);
	}

}
