/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * {@code RepetitionExtension} implements {@link ParameterResolver} to resolve
 * the {@link RepetitionInfo} for the currently executing {@code @RepeatedTest}
 * and also implements {@link TestWatcher} to track the
 * {@linkplain RepetitionInfo#getFailureCount() failure count}.
 *
 * @since 5.0
 */
class RepetitionExtension implements ParameterResolver, TestWatcher {

	private final DefaultRepetitionInfo repetitionInfo;

	RepetitionExtension(DefaultRepetitionInfo repetitionInfo) {
		this.repetitionInfo = repetitionInfo;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == RepetitionInfo.class);
	}

	@Override
	public RepetitionInfo resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return this.repetitionInfo;
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
		this.repetitionInfo.failureCount.incrementAndGet();
	}

}
