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

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherInterceptor;
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

	DefaultLauncherSession(InternalLauncher launcher, LauncherSessionListener listener,
			List<LauncherInterceptor> interceptors) {
		this.launcher = new DelegatingLauncher(InterceptingInternalLauncher.decorate(launcher, interceptors));
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
		if (launcher.isClosed()) {
			launcher.close();
			listener.launcherSessionClosed(this);
		}
	}

	private static class DelegatingLauncher extends DelegatingInternalLauncher<CloseableInternalLauncher>
			implements CloseableInternalLauncher {

		DelegatingLauncher(CloseableInternalLauncher delegate) {
			super(delegate);
		}

		boolean isClosed() {
			return delegate != ClosedLauncher.INSTANCE;
		}

		@Override
		public void close() {
			delegate.close();
			delegate = ClosedLauncher.INSTANCE;
		}
	}

	private static class ClosedLauncher implements CloseableInternalLauncher {

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

		@Override
		public ListenerRegistry<TestExecutionListener> getTestExecutionListenerRegistry() {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public ListenerRegistry<LauncherDiscoveryListener> getLauncherDiscoveryListenerRegistry() {
			throw new PreconditionViolationException("Launcher session has already been closed");
		}

		@Override
		public void close() {
			// do nothing
		}
	}
}
