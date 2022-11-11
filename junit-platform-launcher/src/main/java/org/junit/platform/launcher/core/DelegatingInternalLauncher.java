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
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.10
 */
class DelegatingInternalLauncher<T extends InternalLauncher> implements InternalLauncher {

	protected T delegate;

	DelegatingInternalLauncher(T delegate) {
		this.delegate = delegate;
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
		return delegate.discover(launcherDiscoveryRequest);
	}

	@Override
	public void execute(LauncherDiscoveryRequest launcherDiscoveryRequest, TestExecutionListener... listeners) {
		delegate.execute(launcherDiscoveryRequest, listeners);
	}

	@Override
	public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
		delegate.execute(testPlan, listeners);
	}

	@Override
	public ListenerRegistry<TestExecutionListener> getTestExecutionListenerRegistry() {
		return delegate.getTestExecutionListenerRegistry();
	}

	@Override
	public ListenerRegistry<LauncherDiscoveryListener> getLauncherDiscoveryListenerRegistry() {
		return delegate.getLauncherDiscoveryListenerRegistry();
	}
}
