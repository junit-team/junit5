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

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;

@API(status = INTERNAL, since = "5.4")
public class ExecutionModeUtils {

	public static final String DEFAULT_EXECUTION_MODE_PROPERTY_NAME = "junit.jupiter.execution.parallel.mode.default";

	private static final Logger logger = LoggerFactory.getLogger(ExecutionModeUtils.class);

	static ExecutionMode getDefaultExecutionMode(ConfigurationParameters configParams) {
		Preconditions.notNull(configParams, "ConfigurationParameters must not be null");
		String propertyName = DEFAULT_EXECUTION_MODE_PROPERTY_NAME;

		Optional<String> optional = configParams.get(propertyName);
		String constantName = null;
		if (optional.isPresent()) {
			try {
				constantName = optional.get().trim().toUpperCase();
				ExecutionMode lifecycle = ExecutionMode.valueOf(constantName);
				logger.info(
					() -> String.format("Using default execution mode '%s' set via the '%s' configuration parameter.",
						lifecycle, propertyName));
				return lifecycle;
			}
			catch (Exception ex) {
				// local copy necessary for use in lambda expression
				String constant = constantName;
				logger.warn(() -> String.format(
					"Invalid execution mode '%s' set via the '%s' configuration parameter. "
							+ "Falling back to %s execution mode.",
					constant, propertyName, ExecutionMode.SAME_THREAD.name()));
			}
		}

		return ExecutionMode.SAME_THREAD;
	}

}
