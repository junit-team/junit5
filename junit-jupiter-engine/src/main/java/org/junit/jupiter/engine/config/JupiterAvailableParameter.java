/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import java.util.Arrays;

import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.ConfigurationParameter;

public enum JupiterAvailableParameter implements ConfigurationParameter {

	/** {@code junit.jupiter.conditions.deactivate} */
	CONDITIONS_DEACTIVATE("*", "Provide a pattern for deactivating conditions." + " '*' deactivates all."),

	/** {@code junit.jupiter.execution.parallel.enabled} */
	EXECUTION_PARALLEL_ENABLED("false", "Enable parallel test execution."),

	/** {@code junit.jupiter.execution.parallel.mode.default} */
	EXECUTION_PARALLEL_MODE_DEFAULT(ExecutionMode.SAME_THREAD.name(), "Set the default parallel test execution mode. "
			+ "Available values: " + Arrays.asList(ExecutionMode.values()));

	final String defaultValue;
	final String description;

	JupiterAvailableParameter(String defaultValue, String... description) {
		this.defaultValue = defaultValue;
		this.description = description.length == 0 ? "No description available." : String.join("", description);
	}

	@Override
	public String getKey() {
		return "junit.jupiter." + name().toLowerCase().replace('_', '.');
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
