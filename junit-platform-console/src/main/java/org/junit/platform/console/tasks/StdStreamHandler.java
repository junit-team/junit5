/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.platform.commons.JUnitException;

class StdStreamHandler implements AutoCloseable {
	private PrintStream stdout;
	private PrintStream stderr;

	public StdStreamHandler() {
	}

	private boolean isSameFile(Path path1, Path path2) {
		if (path1 == null || path2 == null)
			return false;
		return (path1.normalize().toAbsolutePath().equals(path2.normalize().toAbsolutePath()));
	}

	public void redirectStdStreams(Path stdoutPath, Path stderrPath) {
		if (isSameFile(stdoutPath, stderrPath)) {
			try {
				PrintStream commonStream = new PrintStream(Files.newOutputStream(stdoutPath), true);
				this.stdout = commonStream;
				this.stderr = commonStream;
			}
			catch (IOException e) {
				throw new JUnitException("Error setting up stream for Stdout and Stderr at path: " + stdoutPath, e);
			}
		}
		else {
			if (stdoutPath != null) {
				try {
					this.stdout = new PrintStream(Files.newOutputStream(stdoutPath), true);
				}
				catch (IOException e) {
					throw new JUnitException("Error setting up stream for Stdout at path: " + stdoutPath, e);
				}
			}

			if (stderrPath != null) {
				try {
					this.stderr = new PrintStream(Files.newOutputStream(stderrPath), true);
				}
				catch (IOException e) {
					throw new JUnitException("Error setting up stream for Stderr at path: " + stderrPath, e);
				}
			}
		}

		if (stdout != null) {
			System.setOut(stdout);
		}
		if (stderr != null) {
			System.setErr(stderr);
		}
	}

	@Override
	public void close() {
		if (stdout != null) {
			stdout.close();
		}
		if (stderr != null) {
			stderr.close();
		}
	}
}
