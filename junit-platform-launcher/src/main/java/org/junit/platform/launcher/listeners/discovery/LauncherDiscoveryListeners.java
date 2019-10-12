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

import java.util.List;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.launcher.LauncherDiscoveryListener;

public class LauncherDiscoveryListeners {

	private LauncherDiscoveryListeners() {
	}

	public static LauncherDiscoveryListener abortOnFailure() {
		return new AbortOnFailureLauncherDiscoveryListener();
	}

	public static LauncherDiscoveryListener logging() {
		return new LoggingLauncherDiscoveryListener();
	}

	public static LauncherDiscoveryListener composite(List<LauncherDiscoveryListener> listeners) {
		Preconditions.notEmpty(listeners, "listeners must not be empty");
		if (listeners.size() == 1) {
			return listeners.get(0);
		}
		return new CompositeLauncherDiscoveryListener(listeners);
	}

}
