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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import joptsimple.OptionSet;

public class JOptSimpleCommandLineOptions implements CommandLineOptions {

	private final OptionSet optionSet;

	public JOptSimpleCommandLineOptions(OptionSet optionSet) {
		this.optionSet = optionSet;
	}

	@Override
	public boolean isDisplayHelp() {
		return optionSet.has("help");
	}

	@Override
	public boolean isExitCodeEnabled() {
		return optionSet.has("enable-exit-code");
	}

	@Override
	public boolean isAnsiColorOutputDisabled() {
		return optionSet.has("disable-ansi-colors");
	}

	@Override
	public boolean isRunAllTests() {
		return optionSet.has("all");
	}

	@Override
	public boolean isHideDetails() {
		return optionSet.has("hide-details");
	}

	@Override
	public Optional<String> getClassnameFilter() {
		return Optional.ofNullable((String) optionSet.valueOf("filter-classname"));
	}

	@Override
	public List<String> getTagsFilter() {
		return toStrings(optionSet.valuesOf("filter-tags"));
	}

	@Override
	public List<String> getArguments() {
		return toStrings(optionSet.nonOptionArguments());
	}

	private List<String> toStrings(List<?> arguments) {
		return arguments.stream().map(String.class::cast).collect(toList());
	}

}
