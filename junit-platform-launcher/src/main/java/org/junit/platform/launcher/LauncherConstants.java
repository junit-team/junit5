/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

public class LauncherConstants {

	public static final String CAPTURE_STDOUT_PROPERTY_NAME = "junit.platform.launcher.capture.stdout";

	public static final String CAPTURE_STDERR_PROPERTY_NAME = "junit.platform.launcher.capture.stderr";

	public static final String CAPTURE_MAX_BUFFER_PROPERTY_NAME = "junit.platform.launcher.capture.maxBuffer";

	public static int CAPTURE_MAX_BUFFER_DEFAULT = 4 * 1024 * 1024;

	private LauncherConstants() {
		/* no-op */
	}

}
