/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.reporting;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.nio.file.Path;
import java.util.ServiceLoader;

import org.apiguardian.api.API;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@API(status = EXPERIMENTAL, since = "5.11")
public class TestReporting extends TestWatcher {

	private final ServiceLoader<VintageReportingService> services;
	private Description description;

	public TestReporting() {
		services = ServiceLoader.load(VintageReportingService.class);
	}

	@Override
	protected void starting(Description description) {
		this.description = description;
	}

	/**
	 * Publish the supplied file and attach it to the current test or container.
	 * <p>
	 * The file will be copied to the report output directory replacing any
	 * potentially existing file with the same name.
	 *
	 * @param file the file to be attached; never {@code null} or blank
	 * @since 5.11
	 */
	public void publishFile(Path file) {
		for (VintageReportingService service : services) {
			service.publishFile(description, file);
		}
	}

}
