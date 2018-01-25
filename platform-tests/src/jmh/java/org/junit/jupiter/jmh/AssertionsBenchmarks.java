/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.jmh;

import org.junit.jupiter.api.Assertions;
import org.openjdk.jmh.annotations.Benchmark;

public class AssertionsBenchmarks {

	@Benchmark
	public void assertTrueWithSimpleStringMessage() {
		Assertions.assertTrue(true, "message");
	}

	@Benchmark
	public void assertTrueWithSimpleStringMessageSupplier() {
		Assertions.assertTrue(true, () -> "message");
	}

}
