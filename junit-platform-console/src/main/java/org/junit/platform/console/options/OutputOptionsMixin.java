/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import picocli.CommandLine;

public class OutputOptionsMixin {

	@CommandLine.Option(names = "--disable-banner", description = "Disable print out of the welcome message.")
	private boolean disableBanner;

	@CommandLine.Option(names = "-disable-banner", hidden = true)
	private boolean disableBanner2;

	@CommandLine.Option(names = "--disable-ansi-colors", description = "Disable ANSI colors in output (not supported by all terminals).")
	private boolean disableAnsiColors;

	@CommandLine.Option(names = "-disable-ansi-colors", hidden = true)
	private boolean disableAnsiColors2;

	public boolean isDisableBanner() {
		return disableBanner || disableBanner2;
	}

	public boolean isDisableAnsiColors() {
		return disableAnsiColors || disableAnsiColors2;
	}
}
