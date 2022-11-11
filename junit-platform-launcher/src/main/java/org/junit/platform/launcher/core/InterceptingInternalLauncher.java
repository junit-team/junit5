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
import java.util.Optional;

import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherInterceptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.10
 */
class InterceptingInternalLauncher extends DelegatingCloseableInternalLauncher<InternalLauncher> {

	static CloseableInternalLauncher decorate(InternalLauncher launcher, List<LauncherInterceptor> interceptors) {
		return composite(interceptors) //
				.map(combinedInterceptor -> (CloseableInternalLauncher) new InterceptingInternalLauncher(launcher,
					combinedInterceptor)) //
				.orElse(new DelegatingCloseableInternalLauncher<>(launcher, () -> {
					// do nothing
				}));
	}

	private static Optional<LauncherInterceptor> composite(List<LauncherInterceptor> interceptors) {
		if (interceptors.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(interceptors.stream() //
				.skip(1) //
				.reduce(interceptors.get(0), (a, b) -> new LauncherInterceptor() {
					@Override
					public void close() {
						try {
							a.close();
						}
						finally {
							b.close();
						}
					}

					@Override
					public <T> T intercept(Invocation<T> invocation) {
						return a.intercept(() -> b.intercept(invocation));
					}
				}));
	}

	private final LauncherInterceptor interceptor;

	private InterceptingInternalLauncher(InternalLauncher delegate, LauncherInterceptor interceptor) {
		super(delegate, interceptor::close);
		this.interceptor = interceptor;
	}

	@Override
	public TestPlan discover(LauncherDiscoveryRequest launcherDiscoveryRequest) {
		return interceptor.intercept(() -> super.discover(launcherDiscoveryRequest));
	}

	@Override
	public void execute(LauncherDiscoveryRequest launcherDiscoveryRequest, TestExecutionListener... listeners) {
		interceptor.intercept(() -> {
			super.execute(launcherDiscoveryRequest, listeners);
			return null;
		});
	}

	@Override
	public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
		interceptor.intercept(() -> {
			super.execute(testPlan, listeners);
			return null;
		});
	}
}
