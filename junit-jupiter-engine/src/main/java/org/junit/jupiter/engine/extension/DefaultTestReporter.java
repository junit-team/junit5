/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.function.ThrowingConsumer;

/**
 * @since 1.12
 */
class DefaultTestReporter implements TestReporter {

	private final ExtensionContext extensionContext;

	DefaultTestReporter(ExtensionContext extensionContext) {
		this.extensionContext = extensionContext;
	}

	@Override
	public void publishEntry(Map<String, String> map) {
		extensionContext.publishReportEntry(map);
	}

	@Override
	public void publishFile(String fileName, ThrowingConsumer<Path> action) {
		extensionContext.publishFile(fileName, action);
	}
}
