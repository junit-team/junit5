/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.junit.platform.launcher.LauncherConstants.DISCOVERY_ISSUE_FAILURE_PHASE_PROPERTY_NAME;

import java.util.Locale;
import java.util.Optional;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * The phase the {@link org.junit.platform.launcher.Launcher} is in.
 *
 * @since 1.13
 */
enum LauncherPhase {

	DISCOVERY, EXECUTION;

	private static final Logger logger = LoggerFactory.getLogger(LauncherPhase.class);

	static Optional<LauncherPhase> getDiscoveryIssueFailurePhase(ConfigurationParameters configurationParameters) {
		return configurationParameters.get(DISCOVERY_ISSUE_FAILURE_PHASE_PROPERTY_NAME, value -> {
			try {
				return LauncherPhase.valueOf(value.toUpperCase(Locale.ROOT));
			}
			catch (Exception e) {
				logger.warn(
					() -> "Ignoring invalid LauncherPhase '%s' set via the '%s' configuration parameter.".formatted(
						value, DISCOVERY_ISSUE_FAILURE_PHASE_PROPERTY_NAME));
				return null;
			}
		});
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
