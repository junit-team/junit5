/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.8
 */
class SessionPerRequestLauncher implements InternalLauncher {

	private final InternalLauncher delegate;
	private final LauncherSessionListener sessionListener;

	SessionPerRequestLauncher(InternalLauncher delegate, LauncherSessionListener sessionListener) {
		this.delegate = delegate;
		this.sessionListener = sessionListener;
	}

	@Override
	public void registerLauncherDiscoveryListeners(LauncherDiscoveryListener... listeners) {
		delegate.registerLauncherDiscoveryListeners(listeners);
	}

	@Override
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		delegate.registerTestExecutionListeners(listeners);
	}

	@Override
	public TestPlan discover(LauncherDiscoveryRequest launcherDiscoveryRequest) {
		try (LauncherSession session = createSession()) {
			return session.getLauncher().discover(launcherDiscoveryRequest);
		}
	}

	@Override
	public void execute(LauncherDiscoveryRequest launcherDiscoveryRequest, TestExecutionListener... listeners) {
		try (LauncherSession session = createSession()) {
			session.getLauncher().execute(launcherDiscoveryRequest, listeners);
		}
	}

	@Override
	public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
		try (LauncherSession session = createSession()) {
			session.getLauncher().execute(testPlan, listeners);
		}
	}

	@Override
	public ListenerRegistry<TestExecutionListener> getTestExecutionListenerRegistry() {
		return delegate.getTestExecutionListenerRegistry();
	}

	@Override
	public ListenerRegistry<LauncherDiscoveryListener> getLauncherDiscoveryListenerRegistry() {
		return delegate.getLauncherDiscoveryListenerRegistry();
	}

	private LauncherSession createSession() {
		return new DefaultLauncherSession(delegate, sessionListener);
	}
}
