/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.gen5.engine.DiscoveryFilter;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestDescriptor;

public class PostDiscoveryFilterMock implements PostDiscoveryFilter {
	private final Function<TestDescriptor, FilterResult> function;
	private final Supplier<String> toString;

	public PostDiscoveryFilterMock(String toString) {
		this(o -> FilterResult.included("always"), () -> toString);
	}

	public PostDiscoveryFilterMock(Function<TestDescriptor, FilterResult> function, Supplier<String> toString) {
		this.function = function;
		this.toString = toString;
	}

	@Override
	public String toString() {
		return toString.get();
	}

	@Override
	public FilterResult filter(TestDescriptor testDescriptor) {
		return function.apply(testDescriptor);
	}
}
