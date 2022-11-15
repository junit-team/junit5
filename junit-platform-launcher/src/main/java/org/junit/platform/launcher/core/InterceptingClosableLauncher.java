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

import static java.util.function.Function.identity;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherInterceptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.10
 */
class InterceptingClosableLauncher extends DelegatingCloseableLauncher<Launcher> {

	static CloseableLauncher decorate(Supplier<Launcher> launcherSupplier, List<LauncherInterceptor> interceptors) {
		Optional<LauncherInterceptor> combinedInterceptor = composite(interceptors);
		Launcher launcher = combinedInterceptor.map(it -> it.intercept(launcherSupplier::get)).orElseGet(
			launcherSupplier);
		return combinedInterceptor //
				.map(it -> (CloseableLauncher) new InterceptingClosableLauncher(launcher, it)) //
				.orElse(new DelegatingCloseableLauncher<>(launcher, identity()));
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

	private InterceptingClosableLauncher(Launcher delegate, LauncherInterceptor interceptor) {
		super(delegate, it -> {
			interceptor.close();
			return it;
		});
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
