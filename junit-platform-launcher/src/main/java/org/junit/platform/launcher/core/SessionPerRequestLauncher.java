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

import java.util.List;
import java.util.function.Supplier;

import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherInterceptor;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.8
 */
class SessionPerRequestLauncher extends DelegatingInternalLauncher<InternalLauncher> {

	private final LauncherSessionListener sessionListener;
	private final Supplier<List<LauncherInterceptor>> interceptorFactory;

	SessionPerRequestLauncher(InternalLauncher delegate, LauncherSessionListener sessionListener,
			Supplier<List<LauncherInterceptor>> interceptorFactory) {
		super(delegate);
		this.sessionListener = sessionListener;
		this.interceptorFactory = interceptorFactory;
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

	private LauncherSession createSession() {
		return new DefaultLauncherSession(delegate, sessionListener, interceptorFactory.get());
	}
}
