/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.opentest4j.MultipleFailuresError;

/**
 * {@code AssertAll} is a collection of utility methods that support asserting
 * multiple conditions in tests at once.
 *
 * @since 5.0
 */
class AssertAll {

	static void assertAll(Executable... executables) {
		assertAll(null, executables);
	}

	static void assertAll(String heading, Executable... executables) {
		Preconditions.notEmpty(executables, "executables array must not be null or empty");
		Preconditions.containsNoNullElements(executables, "individual executables must not be null");
		assertAll(heading, Arrays.stream(executables));
	}

	static void assertAll(Stream<Executable> executables) {
		assertAll(null, executables);
	}

	static void assertAll(String heading, Stream<Executable> executables) {
		Preconditions.notNull(executables, "executables must not be null");

		List<Throwable> failures = new ArrayList<>();
		executables.forEach(executable -> {
			try {
				executable.execute();
			}
			catch (AssertionError assertionError) {
				failures.add(assertionError);
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});

		MultipleFailuresError multipleFailuresError = new MultipleFailuresError(heading, failures);

		if (multipleFailuresError.hasFailures()) {
			throw multipleFailuresError;
		}
	}

}
