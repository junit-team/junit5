/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy;

import java.util.Objects;

/** Tool interface. */
public interface Tool {

	String getName();

	default String getProgram() {
		return getName();
	}

	String getVersion();

	default int run(Object... args) {
		Objects.requireNonNull(args, "args must not be null");
		return run(Configuration.of(args)).getExitCode();
	}

	Result run(Configuration configuration);
}
