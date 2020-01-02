/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.Writer;

import org.apiguardian.api.API;

/**
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public interface CommandLineOptionsParser {

	CommandLineOptions parse(String... arguments);

	void printHelp(Writer writer);

}
