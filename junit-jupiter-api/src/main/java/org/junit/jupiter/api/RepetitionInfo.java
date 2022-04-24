/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * {@code RepetitionInfo} is used to inject information about the current
 * repetition of a repeated test into {@code @RepeatedTest}, {@code @BeforeEach},
 * and {@code @AfterEach} methods.
 *
 * <p>If a method parameter is of type {@code RepetitionInfo}, JUnit will
 * supply an instance of {@code RepetitionInfo} corresponding to the current
 * repeated test as the value for the parameter.
 *
 * <p><strong>WARNING</strong>: {@code RepetitionInfo} cannot be injected into
 * a {@code @BeforeEach} or {@code @AfterEach} method if the corresponding test
 * method is not a {@code @RepeatedTest}. Any attempt to do so will result in a
 * {@link org.junit.jupiter.api.extension.ParameterResolutionException
 * ParameterResolutionException}.
 *
 * @since 5.0
 * @see RepeatedTest
 * @see TestInfo
 */
@API(status = STABLE, since = "5.0")
public interface RepetitionInfo {

	/**
	 * Get the current repetition of the corresponding
	 * {@link RepeatedTest @RepeatedTest} method.
	 */
	int getCurrentRepetition();

	/**
	 * Get the total number of repetitions of the corresponding
	 * {@link RepeatedTest @RepeatedTest} method.
	 *
	 * @see RepeatedTest#value
	 */
	int getTotalRepetitions();

	/**
	 * Get The flag determining whether the repeated tests will stop after first
	 * fail occurring of the corresponding
	 * {@link RepeatedTest @RepeatedTest} method.
	 *
	 * @see RepeatedTest#stopFirstFail
	 */
	// CS304 Issue link: https://github.com/junit-team/junit5/issues/2119
	boolean getStopFlag();
}
