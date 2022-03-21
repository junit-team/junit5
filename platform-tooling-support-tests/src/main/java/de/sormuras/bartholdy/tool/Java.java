/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy.tool;

import java.nio.file.Path;

import de.sormuras.bartholdy.Bartholdy;

/**
 * You can use the {@code java} command to launch a Java application.
 *
 * @see <a href="https://docs.oracle.com/javase/10/tools/java.htm">java</a>
 */
public class Java extends AbstractTool {

	@Override
	public Path getHome() {
		return Bartholdy.currentJdkHome();
	}

	@Override
	public String getName() {
		return "java";
	}

	@Override
	public final String getProgram() {
		return "java";
	}

	@Override
	public String getVersion() {
		return Runtime.version().toString();
	}
}
