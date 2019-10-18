/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners.discovery;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.launcher.LauncherDiscoveryListener;

@API(status = API.Status.EXPERIMENTAL, since = "1.6")
public class LauncherDiscoveryListeners {

	public static final String DEFAULT_DISCOVERY_LISTENER_CONFIGURATION_PROPERTY_NAME = "junit.platform.discovery.listener.default";

	private LauncherDiscoveryListeners() {
	}

	public static LauncherDiscoveryListener abortOnFailure() {
		return new AbortOnFailureLauncherDiscoveryListener();
	}

	public static LauncherDiscoveryListener logging() {
		return new LoggingLauncherDiscoveryListener();
	}

	@API(status = API.Status.INTERNAL, since = "1.6")
	public static LauncherDiscoveryListener composite(List<LauncherDiscoveryListener> listeners) {
		Preconditions.notEmpty(listeners, "listeners must not be empty");
		Preconditions.containsNoNullElements(listeners, "listeners must not contain any null elements");
		if (listeners.size() == 1) {
			return listeners.get(0);
		}
		return new CompositeLauncherDiscoveryListener(listeners);
	}

	@API(status = API.Status.INTERNAL, since = "1.6")
	public static LauncherDiscoveryListener fromConfigurationParameter(
			ConfigurationParameters configurationParameters) {
		return configurationParameters.get(DEFAULT_DISCOVERY_LISTENER_CONFIGURATION_PROPERTY_NAME,
			defaultListenerType -> //
			Arrays.stream(LauncherDiscoveryListenerType.values()) //
					.filter(type -> type.parameterValue.equalsIgnoreCase(defaultListenerType)) //
					.findFirst() //
					.map(type -> type.creator.get()) //
					.orElseThrow(() -> {
						String allowedValues = Arrays.stream(LauncherDiscoveryListenerType.values()) //
								.map(type -> type.parameterValue) //
								.collect(joining("', '", "'", "'"));
						return new JUnitException("Invalid value of configuration parameter '"
								+ DEFAULT_DISCOVERY_LISTENER_CONFIGURATION_PROPERTY_NAME + "': " //
								+ defaultListenerType + " (allowed are " + allowedValues + ")");
					})) //
				.orElseGet(LauncherDiscoveryListeners::abortOnFailure);
	}

	private enum LauncherDiscoveryListenerType {

		LOGGING("logging", LauncherDiscoveryListeners::logging),

		ABORT_ON_FAILURE("abortOnFailure", LauncherDiscoveryListeners::abortOnFailure);

		private final String parameterValue;
		private final Supplier<LauncherDiscoveryListener> creator;

		LauncherDiscoveryListenerType(String parameterValue, Supplier<LauncherDiscoveryListener> creator) {
			this.parameterValue = parameterValue;
			this.creator = creator;
		}

	}

}
