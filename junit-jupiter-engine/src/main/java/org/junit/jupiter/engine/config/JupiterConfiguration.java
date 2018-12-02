/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.parallel.ExecutionMode;

@API(status = INTERNAL, since = "5.4")
public interface JupiterConfiguration {

	String DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME = "junit.jupiter.conditions.deactivate";
	String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = "junit.jupiter.execution.parallel.enabled";
	String DEFAULT_EXECUTION_MODE_PROPERTY_NAME = "junit.jupiter.execution.parallel.mode.default";
	String EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME = "junit.jupiter.extensions.autodetection.enabled";
	String DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME = "junit.jupiter.testinstance.lifecycle.default";
	String DEACTIVATE_ALL_CONDITIONS_PATTERN = ClassNamePatternParameterConverter.DEACTIVATE_ALL_PATTERN;

	Optional<String> getRawConfigurationParameter(String key);

	boolean isParallelExecutionEnabled();

	boolean isExtensionAutoDetectionEnabled();

	ExecutionMode getDefaultExecutionMode();

	TestInstance.Lifecycle getDefaultTestInstanceLifecycle();

	Predicate<ExecutionCondition> getExecutionConditionFilter();

}
