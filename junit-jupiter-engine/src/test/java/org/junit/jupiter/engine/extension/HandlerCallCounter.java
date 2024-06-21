/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

class HandlerCallCounter {
	private int beforeAllCalls;
	private int beforeEachCalls;
	private int afterEachCalls;
	private int afterAllCalls;

	public HandlerCallCounter() {
		reset();
	}

	public void reset() {
		this.beforeAllCalls = 0;
		this.beforeEachCalls = 0;
		this.afterEachCalls = 0;
		this.afterAllCalls = 0;
	}

	public void incrementBeforeAllCalls() {
		beforeAllCalls++;
	}

	public void incrementBeforeEachCalls() {
		beforeEachCalls++;
	}

	public void incrementAfterEachCalls() {
		afterEachCalls++;
	}

	public void incrementAfterAllCalls() {
		afterAllCalls++;
	}

	public int getBeforeAllCalls() {
		return beforeAllCalls;
	}

	public int getBeforeEachCalls() {
		return beforeEachCalls;
	}

	public int getAfterEachCalls() {
		return afterEachCalls;
	}

	public int getAfterAllCalls() {
		return afterAllCalls;
	}
}
