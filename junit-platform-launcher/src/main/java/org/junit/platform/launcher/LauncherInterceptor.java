/*
 * Copyright 2015-2023 the original author or authors.
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

/**
 * Interceptor for test discovery and execution by a {@link Launcher}.
 *
 * <p>Interceptors are instantiated once per {@link LauncherSession} and closed
 * when the session is about to be closed.
 *
 * @since 1.10
 * @see Launcher
 * @see LauncherSession
 * @see LauncherConstants#ENABLE_LAUNCHER_INTERCEPTORS
 */
@API(status = EXPERIMENTAL, since = "1.10")
public interface LauncherInterceptor {

	/**
	 * Intercept the supplied invocation.
	 *
	 * <p>Implementations must call {@link Invocation#proceed()} exactly once.
	 *
	 * @param invocation the intercepted invocation; never {@code null}
	 * @return the result of the invocation
	 */
	<T> T intercept(Invocation<T> invocation);

	/**
	 * Closes this interceptor.
	 *
	 * <p>Any resources held by this interceptor should be released by this
	 * method.
	 */
	void close();

	/**
	 * An invocation that can be intercepted.
	 *
	 * <p>This interface is not intended to be implemented by clients.
	 */
	interface Invocation<T> {
		T proceed();
	}
}
