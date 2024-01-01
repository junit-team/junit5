/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static picocli.CommandLine.Help.defaultColorScheme;
import static picocli.CommandLine.Spec.Target.MIXEE;

import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

class OutputOptionsMixin {

	@Spec(MIXEE)
	CommandSpec commandSpec;

	@Option(names = "--disable-banner", description = "Disable print out of the welcome message.")
	private boolean disableBanner;

	@Option(names = "-disable-banner", hidden = true)
	private boolean disableBanner2;

	private boolean disableAnsiColors;

	public boolean isDisableBanner() {
		return disableBanner || disableBanner2;
	}

	public boolean isDisableAnsiColors() {
		return disableAnsiColors;
	}

	@Option(names = "--disable-ansi-colors", description = "Disable ANSI colors in output (not supported by all terminals).")
	public void setDisableAnsiColors(boolean disableAnsiColors) {
		if (disableAnsiColors) {
			commandSpec.commandLine().setColorScheme(defaultColorScheme(Ansi.OFF));
		}
		this.disableAnsiColors = disableAnsiColors;
	}

	@Option(names = "-disable-ansi-colors", hidden = true)
	public void setDisableAnsiColors2(boolean disableAnsiColors) {
		setDisableAnsiColors(disableAnsiColors);
	}
}
