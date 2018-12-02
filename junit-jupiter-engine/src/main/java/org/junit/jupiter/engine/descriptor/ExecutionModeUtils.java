/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.ConfigurationParameters;

@API(status = INTERNAL, since = "5.4")
public class ExecutionModeUtils {

	public static final String DEFAULT_EXECUTION_MODE_PROPERTY_NAME = "junit.jupiter.execution.parallel.mode.default";

	static ExecutionMode getDefaultExecutionMode(ConfigurationParameters configParams) {
		EnumConfigurationParameterConverter<ExecutionMode> converter = new EnumConfigurationParameterConverter<>(
			ExecutionMode.class, "parallel execution mode");
		return converter.get(configParams, DEFAULT_EXECUTION_MODE_PROPERTY_NAME, ExecutionMode.SAME_THREAD);
	}

}
