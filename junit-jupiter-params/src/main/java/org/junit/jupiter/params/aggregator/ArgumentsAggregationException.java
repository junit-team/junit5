/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;

/**
 * {@code ArgumentsAggregationException} is an exception thrown by an
 * {@link ArgumentsAggregator} when an error occurs while aggregating
 * arguments.
 *
 * @since 5.2
 * @see ArgumentsAggregator
 */
@API(status = EXPERIMENTAL, since = "5.2")
public class ArgumentsAggregationException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ArgumentsAggregationException(String message) {
		super(message);
	}

	public ArgumentsAggregationException(String message, Throwable cause) {
		super(message, cause);
	}

}
