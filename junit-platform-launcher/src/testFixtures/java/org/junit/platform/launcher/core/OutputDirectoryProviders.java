/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.nio.file.Path;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;

public class OutputDirectoryProviders {

	public static OutputDirectoryProvider dummyOutputDirectoryProvider() {
		return new HierarchicalOutputDirectoryProvider(() -> {
			throw new JUnitException("This should not be called; use a real provider instead");
		});
	}

	public static OutputDirectoryProvider hierarchicalOutputDirectoryProvider(Path rootDir) {
		return new HierarchicalOutputDirectoryProvider(() -> rootDir);
	}

	private OutputDirectoryProviders() {
	}
}
