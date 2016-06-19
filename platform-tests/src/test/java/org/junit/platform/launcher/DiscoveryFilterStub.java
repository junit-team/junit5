/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.FilterResult;

/**
 * @since 5.0
 */
public class DiscoveryFilterStub<T> extends FilterStub<T> implements DiscoveryFilter<T> {

	public DiscoveryFilterStub(String toString) {
		super(toString);
	}

	public DiscoveryFilterStub(Function<T, FilterResult> function, Supplier<String> toString) {
		super(function, toString);
	}

}
