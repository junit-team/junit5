/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
enum Color {

	NONE(0),

	BLACK(30),

	RED(31),

	GREEN(32),

	YELLOW(33),

	BLUE(34),

	PURPLE(35),

	CYAN(36),

	WHITE(37);

	static Color valueOf(TestExecutionResult result) {
		switch (result.getStatus()) {
			case SUCCESSFUL:
				return Color.SUCCESSFUL;
			case ABORTED:
				return Color.ABORTED;
			case FAILED:
				return Color.FAILED;
			default:
				return Color.NONE;
		}
	}

	static Color valueOf(TestIdentifier testIdentifier) {
		return testIdentifier.isContainer() ? CONTAINER : TEST;
	}

	static final Color SUCCESSFUL = GREEN;

	static final Color ABORTED = YELLOW;

	static final Color FAILED = RED;

	static final Color SKIPPED = PURPLE;

	static final Color CONTAINER = CYAN;

	static final Color TEST = BLUE;

	static final Color DYNAMIC = PURPLE;

	static final Color REPORTED = WHITE;

	private final String ansiString;

	Color(int ansiCode) {
		this.ansiString = "\u001B[" + ansiCode + "m";
	}

	@Override
	public String toString() {
		return this.ansiString;
	}

}
