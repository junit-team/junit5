/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DynamicTest {

	private final String name;
	private final Executable executable;

	public DynamicTest(String name, Executable executable) {
		this.name = name;
		this.executable = executable;
	}

	public String getName() {
		return name;
	}

	public Executable getExecutable() {
		return executable;
	}

	public static <T extends Object> Stream<DynamicTest> streamFrom(Iterator<T> generator,
			Function<T, String> nameSupplier, Consumer<T> assertion) {
		Stream<T> targetStream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(generator, Spliterator.ORDERED), false);
		return targetStream.map(element -> {
			String testName = nameSupplier.apply(element);
			Executable testExecutable = () -> assertion.accept(element);
			return new DynamicTest(testName, testExecutable);
		});

	}
}
