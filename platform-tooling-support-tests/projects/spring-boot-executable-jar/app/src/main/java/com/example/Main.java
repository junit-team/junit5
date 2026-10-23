/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package com.example;

import org.junit.platform.console.ConsoleLauncher;

public class Main {

	public static void main(String[] args) {
		// Selecting a package forces classpath scanning via `jar:nested:` URIs of both
		// BOOT-INF/classes (ExampleTests) and BOOT-INF/lib/tests-1.0-SNAPSHOT.jar (LibraryTests)
		ConsoleLauncher.main("execute", "--select-package=com.example", "--details=tree", "--details-theme=ascii",
			"--disable-banner", "--disable-ansi-colors");
	}
}
