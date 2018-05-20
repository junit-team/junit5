/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;

import java.util.Optional;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * Collection of utilities for retrieving the test instance lifecycle mode.
 *
 * @since 5.0
 * @see TestInstance
 * @see TestInstance.Lifecycle
 */
final class TestInstanceLifecycleUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestInstanceLifecycleUtils.class);

	private TestInstanceLifecycleUtils() {
		/* no-op */
	}

	static TestInstance.Lifecycle getTestInstanceLifecycle(Class<?> testClass, ConfigurationParameters configParams) {
		Preconditions.notNull(testClass, "testClass must not be null");
		Preconditions.notNull(configParams, "ConfigurationParameters must not be null");

		// @formatter:off
		return AnnotationUtils.findAnnotation(testClass, TestInstance.class)
				.map(TestInstance::value)
				.orElseGet(() -> getDefaultTestInstanceLifecycle(configParams));
		// @formatter:on
	}

	static TestInstance.Lifecycle getDefaultTestInstanceLifecycle(ConfigurationParameters configParams) {
		Preconditions.notNull(configParams, "ConfigurationParameters must not be null");
		String propertyName = DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;

		Optional<String> optional = configParams.get(propertyName);
		String constantName = null;
		if (optional.isPresent()) {
			try {
				constantName = optional.get().trim().toUpperCase();
				Lifecycle lifecycle = TestInstance.Lifecycle.valueOf(constantName);
				logger.info(() -> String.format(
					"Using default test instance lifecycle mode '%s' set via the '%s' configuration parameter.",
					lifecycle, propertyName));
				return lifecycle;
			}
			catch (Exception ex) {
				// local copy necessary for use in lambda expression
				String constant = constantName;
				logger.warn(() -> String.format(
					"Invalid test instance lifecycle mode '%s' set via the '%s' configuration parameter. "
							+ "Falling back to %s lifecycle semantics.",
					constant, propertyName, Lifecycle.PER_METHOD.name()));
			}
		}

		return Lifecycle.PER_METHOD;
	}

}
