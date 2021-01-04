/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * Register an implementation of this interface with the
 * {@link LauncherConfig.Builder#addLauncherSessionListeners LauncherConfig}
 * passed to
 * {@link LauncherFactory#openSession(LauncherConfig) LauncherFactory} or via
 * ServiceLoader to be notified when a {@link LauncherSession} is opened and
 * closed.
 *
 * <p>All methods in this class have empty <em>default</em> implementations.
 * Subclasses may therefore override one or more of these methods to be notified
 * of the selected events.
 *
 * <p>The methods declared in this interface are called by the {@link Launcher}
 * or {@link LauncherSession} created via the {@link LauncherFactory}.
 *
 * @see LauncherSession
 * @see LauncherConfig.Builder#addLauncherSessionListeners
 * @see LauncherFactory
 * @since 1.8
 */
@API(status = EXPERIMENTAL, since = "1.8")
public interface LauncherSessionListener {

	/**
	 * No-op implementation of {@code LauncherSessionListener}
	 */
	LauncherSessionListener NOOP = new LauncherSessionListener() {
	};

	/**
	 * Called when a launcher session was opened.
	 *
	 * @param session the opened session
	 */
	default void launcherSessionOpened(LauncherSession session) {
	}

	/**
	 * Called when a launcher session was closed.
	 *
	 * @param session the closed session
	 */
	default void launcherSessionClosed(LauncherSession session) {
	}

}
