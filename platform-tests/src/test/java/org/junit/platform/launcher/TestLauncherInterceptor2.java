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

public class TestLauncherInterceptor2 implements LauncherInterceptor {

	@Override
	public <T> T intercept(Invocation<T> invocation) {
		return invocation.proceed();
	}

	@Override
	public void close() {
	}
}
