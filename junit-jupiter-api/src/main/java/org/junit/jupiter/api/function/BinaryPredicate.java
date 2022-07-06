/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.function;

import java.util.Comparator;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.EXPERIMENTAL, since = "5.10")
public interface BinaryPredicate<T> {

	boolean test(T expected, T actual);

	default BinaryPredicate<T> not() {
		return not(this);
	}

	static <T extends Comparable<T>> BinaryPredicate<T> compare() {
		return compare(Comparable::compareTo);
	}

	static <T> BinaryPredicate<T> compare(Comparator<T> compare) {
		return (expected, actual) -> compare.compare(expected, actual) == 0;
	}

	static <T> BinaryPredicate<T> not(BinaryPredicate<T> predicate) {
		return (expected, actual) -> !predicate.test(expected, actual);
	}

}
