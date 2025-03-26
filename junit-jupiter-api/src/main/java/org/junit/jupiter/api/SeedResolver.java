/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import java.util.Optional;
import java.util.function.Function;

import org.junit.platform.commons.logging.Logger;

class SeedResolver {

	static final String EXECUTION_ORDER_RANDOM_SEED_PROPERTY = "junit.jupiter.execution.order.random.seed";
	static final long DEFAULT_SEED = System.nanoTime();

	static long resolveSeed(Function<String, Optional<String>> configLookup, Logger logger) {
		return getCustomSeed(configLookup, logger).orElse(DEFAULT_SEED);
	}

	private static Optional<Long> getCustomSeed(Function<String, Optional<String>> configLookup, Logger logger) {
		return configLookup.apply(EXECUTION_ORDER_RANDOM_SEED_PROPERTY).map(param -> parseAndLogSeed(param, logger));
	}

	private static Long parseAndLogSeed(String param, Logger logger) {
		try {
			logCustomSeedUsage(param, logger);
			return Long.valueOf(param);
		}
		catch (NumberFormatException ex) {
			logSeedFallbackWarning(param, logger, ex);
			return null;
		}
	}

	private static void logCustomSeedUsage(String param, Logger logger) {
		logger.config(() -> String.format("Using custom seed for configuration parameter [%s] with value [%s].",
			EXECUTION_ORDER_RANDOM_SEED_PROPERTY, param));
	}

	private static void logSeedFallbackWarning(String param, Logger logger, NumberFormatException ex) {
		logger.warn(ex,
			() -> String.format(
				"Failed to convert configuration parameter [%s] with value [%s] to a long. "
						+ "Using default seed [%s] as fallback.",
				EXECUTION_ORDER_RANDOM_SEED_PROPERTY, param, DEFAULT_SEED));
	}
}
