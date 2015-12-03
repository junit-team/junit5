/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.options;

import java.util.List;
import java.util.Optional;

import joptsimple.OptionSet;

class JOptSimpleCommandLineOptions implements CommandLineOptions {

	private final AvailableOptions options;
	private final OptionSet detectedOptions;

	JOptSimpleCommandLineOptions(AvailableOptions options, OptionSet detectedOptions) {
		this.options = options;
		this.detectedOptions = detectedOptions;
	}

	@Override
	public boolean isDisplayHelp() {
		return detectedOptions.has(options.help);
	}

	@Override
	public boolean isExitCodeEnabled() {
		return detectedOptions.has(options.enableExitCode);
	}

	@Override
	public boolean isAnsiColorOutputDisabled() {
		return detectedOptions.has(options.disableAnsiColors);
	}

	@Override
	public boolean isRunAllTests() {
		return detectedOptions.has(options.runAllTests);
	}

	@Override
	public boolean isHideDetails() {
		return detectedOptions.has(options.hideDetails);
	}

	@Override
	public Optional<String> getClassnameFilter() {
		return Optional.ofNullable(detectedOptions.valueOf(options.classnameFilter));
	}

	@Override
	public List<String> getTagsFilter() {
		return detectedOptions.valuesOf(options.tagFilter);
	}

	@Override
	public List<String> getArguments() {
		return detectedOptions.valuesOf(options.arguments);
	}

}
