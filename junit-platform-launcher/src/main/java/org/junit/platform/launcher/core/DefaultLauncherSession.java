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

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.8
 */
class DefaultLauncherSession implements LauncherSession {

	private final DelegatingLauncher launcher;
	private final LauncherSessionListener listener;

	DefaultLauncherSession(Launcher launcher, LauncherSessionListener listener) {
		this.launcher = new DelegatingLauncher(launcher);
		this.listener = listener;
		listener.launcherSessionOpened(this);
	}

	@Override
	public Launcher getLauncher() {
		return launcher;
	}

	LauncherSessionListener getListener() {
		return listener;
	}

	@Override
	public void close() {
		if (launcher.getDelegate() != ClosedLauncher.INSTANCE) {
			launcher.setDelegate(ClosedLauncher.INSTANCE);
			listener.launcherSessionClosed(this);
		}
	}

	private static class DelegatingLauncher implements Launcher {

		private Launcher delegate;

		DelegatingLauncher(Launcher delegate) {
			this.delegate = delegate;
		}

		public Launcher getDelegate() {
			return delegate;
		}

		public void setDelegate(Launcher delegate) {
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
	}

	private static class ClosedLauncher implements Launcher {

		static final ClosedLauncher INSTANCE = new ClosedLauncher();

		private ClosedLauncher() {
		}

		@Override
		public void registerLauncherDiscoveryListeners(LauncherDiscoveryListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public void registerTestExecutionListeners(TestExecutionListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public TestPlan discover(LauncherDiscoveryRequest launcherDiscoveryRequest) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public void execute(LauncherDiscoveryRequest launcherDiscoveryRequest, TestExecutionListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}
	}
}
