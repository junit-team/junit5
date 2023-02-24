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

public class BannerPrintingCommand<T> implements Command<T> {

	private final Command<T> delegate;

	public BannerPrintingCommand(Command<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public CommandResult<T> run(PrintWriter out, PrintWriter err) throws Exception {
		displayBanner(out);
		return delegate.run(out, err);
	}

	void displayBanner(PrintWriter out) {
		out.println();
		out.println("Thanks for using JUnit! Support its development at https://junit.org/sponsoring");
		out.println();
	}
}
