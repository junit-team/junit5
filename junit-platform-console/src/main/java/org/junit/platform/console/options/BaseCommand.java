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

import java.io.PrintWriter;
import java.util.concurrent.Callable;

import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

abstract class BaseCommand<T> implements Callable<T> {

	@Spec
	CommandSpec commandSpec;

	@Mixin
	OutputOptionsMixin banner;

	@Option(names = { "-h", "--help" }, usageHelp = true)
	private boolean helpRequested;

	@Override
	public final T call() {
		PrintWriter out = getOut();
		if (!banner.isDisableBanner()) {
			displayBanner(out);
		}
		return execute(out);
	}

	private PrintWriter getOut() {
		return commandSpec.commandLine().getOut();
	}

	private void displayBanner(PrintWriter out) {
		out.println();
		out.println("Thanks for using JUnit! Support its development at https://junit.org/sponsoring");
		out.println();
	}

	protected abstract T execute(PrintWriter out);
}
