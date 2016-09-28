/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import org.junit.platform.engine.TestExecutionResult;

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
				return GREEN;
			case ABORTED:
				return YELLOW;
			case FAILED:
				return RED;
			default:
				return NONE;
		}
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
