/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.execution;

import java.nio.file.Path;

import org.junit.runner.Description;
import org.junit.vintage.reporting.VintageReportingService;

public class JUnitPlatformVintageReportingService implements VintageReportingService {
	@Override
	public void publishFile(Description description, Path file) {
		RunListenerAdapter.CURRENT.get().publishFile(description, file);
	}
}
