/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
@API(Internal)
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
				return successful();
			case ABORTED:
				return aborted();
			case FAILED:
				return failed();
			default:
				return NONE;
		}
	}

	static Color valueOf(TestIdentifier testIdentifier) {
		return testIdentifier.isContainer() ? container() : test();
	}

	static Color successful() {
		return GREEN;
	}

	static Color aborted() {
		return YELLOW;
	}

	static Color failed() {
		return RED;
	}

	static Color skipped() {
		return PURPLE;
	}

	static Color container() {
		return CYAN;
	}

	static Color test() {
		return BLUE;
	}

	static Color dynamic() {
		return PURPLE;
	}

	static Color reported() {
		return WHITE;
	}

	private final int ansiCode;

	Color(int ansiCode) {
		this.ansiCode = ansiCode;
	}

	@Override
	public String toString() {
		return "\u001B[" + this.ansiCode + "m";
	}
}
