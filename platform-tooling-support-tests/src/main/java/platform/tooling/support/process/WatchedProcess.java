/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.process;

public class WatchedProcess {

	private final Process process;
	private final WatchedOutput out;
	private final WatchedOutput err;

	WatchedProcess(Process process, WatchedOutput out, WatchedOutput err) {
		this.process = process;
		this.out = out;
		this.err = err;
	}

	ProcessResult waitFor() throws InterruptedException {
		try {
			int exitCode;
			try {
				try {
					exitCode = process.waitFor();
				}
				catch (InterruptedException e) {
					process.destroyForcibly();
					throw e;
				}
			}
			finally {
				try {
					out.join();
				}
				finally {
					err.join();
				}
			}
			return new ProcessResult(exitCode, out.getStreamAsString(), err.getStreamAsString());
		}
		finally {
			process.destroyForcibly();
		}
	}
}
