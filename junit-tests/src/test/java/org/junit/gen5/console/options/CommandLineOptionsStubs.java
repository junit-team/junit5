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

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

import java.util.Optional;

public class CommandLineOptionsStubs {

	public static CommandLineOptions runAllTestCommandLineOptions() {
		CommandLineOptions options = validCommandLineOptions();
		when(options.isRunAllTests()).thenReturn(true);
		return options;
	}

	public static CommandLineOptions validCommandLineOptions() {
		CommandLineOptions options = mock(CommandLineOptions.class);
		when(options.getClassnameFilter()).thenReturn(Optional.empty());
		when(options.getTagsFilter()).thenReturn(emptyList());
		when(options.getArguments()).thenReturn(emptyList());
		return options;
	}
}
