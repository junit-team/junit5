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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.TestExecutionResult;

/**
 * @since 1.0
 */
@API(Internal)
enum Theme {

	/**
	 * ASCII 7-bit characters form the tree branch.
	 * <p>
	 * <pre class="code">
	 * +-- engine alpha
	 * | '-- container BEGIN
	 * |   +-- test 00 [OK]
	 * |   '-- test 01 [OK]
	 * '-- engine omega
	 *   +-- container END
	 *   | +-- test 10 [OK]
	 *   | '-- test 11 [A] aborted
	 *   '-- container FINAL
	 *     +-- skipped [S] because
	 *     '-- failing [X] BäMM
	 * </pre>
	 */
	ASCII(".", "| ", "+--", "'--", "[OK]", "[A]", "[X]", "[S]"),

	/**
	 * Unicode (extended ASCII) characters are used to display the test execution tree.
	 * <p>
	 * <pre class="code">
	 * ├─ engine alpha ✔
	 * │  └─ container BEGIN ✔
	 * │     ├─ test 00 ✔
	 * │     └─ test 01 ✔
	 * └─ engine omega ✔
	 *    ├─ container END ✔
	 *    │  ├─ test 10 ✔
	 *    │  └─ test 11 ■ aborted
	 *    └─ container FINAL ✔
	 *       ├─ skipped ↷ because
	 *       └─ failing ✘ BäMM
	 * </pre>
	 */
	UTF_8("╷", "│  ", "├─", "└─", "✔", "■", "✘", "↷");

	static Theme valueOf(Charset charset) {
		if (StandardCharsets.UTF_8.equals(charset)) {
			return UTF_8;
		}
		return ASCII;
	}

	private final String[] tiles;
	private final String blank;

	Theme(String... tiles) {
		this.tiles = tiles;
		this.blank = new String(new char[vertical().length()]).replace('\0', ' ');
	}

	String root() {
		return tiles[0];
	}

	String vertical() {
		return tiles[1];
	}

	String blank() {
		return blank;
	}

	String entry() {
		return tiles[2];
	}

	String end() {
		return tiles[3];
	}

	String successful() {
		return tiles[4];
	}

	String aborted() {
		return tiles[5];
	}

	String failed() {
		return tiles[6];
	}

	String skipped() {
		return tiles[7];
	}

	String status(TestExecutionResult result) {
		switch (result.getStatus()) {
			case SUCCESSFUL:
				return successful();
			case ABORTED:
				return aborted();
			case FAILED:
				return failed();
			default:
				return result.getStatus().name();
		}
	}
}
