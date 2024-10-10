/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import org.apiguardian.api.API;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Enumeration of argument count validation modes for {@link ParameterizedTest @ParameterizedTest}.
 *
 * <p>When an {@link ArgumentsSource} provides more arguments than declared by the test method,
 * there might be a bug in the test method or the {@link ArgumentsSource}.
 * By default, the additional arguments are ignored.
 * {@link ArgumentCountValidationMode} allows you to control how additional arguments are handled.
 *
 * @since 5.12
 * @see ParameterizedTest
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.12")
public enum ArgumentCountValidationMode {
	/**
	 * Use the default cleanup mode.
	 *
	 * <p>The default cleanup mode may be changed via the
	 * {@value ParameterizedTestExtension#ARGUMENT_COUNT_VALIDATION_KEY} configuration parameter
	 * (see the User Guide for details on configuration parameters).
	 */
	DEFAULT,

	/**
	 * Use the "none" argument count validation mode.
	 *
	 * <p>When there are more arguments provided than declared by the test method,
	 * these additional arguments are ignored.
	 */
	NONE,

	/**
	 * Use the strict argument count validation mode.
	 *
	 * <p>When there are more arguments provided than declared by the test method, this raises an error.
	 */
	STRICT,
}
