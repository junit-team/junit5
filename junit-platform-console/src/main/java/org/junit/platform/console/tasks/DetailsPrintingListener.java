/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import java.util.regex.Pattern;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

interface DetailsPrintingListener extends TestExecutionListener {

	Pattern LINE_START_PATTERN = Pattern.compile("(?m)^");

	void listTests(TestPlan testPlan);

	static String indented(String message, String indentation) {
		return LINE_START_PATTERN.matcher(message).replaceAll(indentation).trim();
	}
}
