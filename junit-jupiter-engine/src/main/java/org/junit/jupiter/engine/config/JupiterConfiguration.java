/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.util.ClassNamePatternFilterUtils;

/**
 * @since 5.4
 */
@API(status = INTERNAL, since = "5.4")
public interface JupiterConfiguration {

	String DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME = "junit.jupiter.conditions.deactivate";
	String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = "junit.jupiter.execution.parallel.enabled";
	String DEFAULT_EXECUTION_MODE_PROPERTY_NAME = "junit.jupiter.execution.parallel.mode.default";
	String DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME = "junit.jupiter.execution.parallel.mode.classes.default";
	String EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME = "junit.jupiter.extensions.autodetection.enabled";
	String DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME = "junit.jupiter.testinstance.lifecycle.default";
	String DEACTIVATE_ALL_CONDITIONS_PATTERN = ClassNamePatternFilterUtils.DEACTIVATE_ALL_PATTERN;
	String DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME = "junit.jupiter.displayname.generator.default";

	String DEFAULT_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.default";
	String DEFAULT_TESTABLE_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.testable.method.default";
	String DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.test.method.default";
	String DEFAULT_TEST_TEMPLATE_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.testtemplate.method.default";
	String DEFAULT_TEST_FACTORY_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.testfactory.method.default";
	String DEFAULT_LIFECYCLE_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.lifecycle.method.default";
	String DEFAULT_BEFORE_ALL_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.beforeall.method.default";
	String DEFAULT_BEFORE_EACH_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.beforeeach.method.default";
	String DEFAULT_AFTER_EACH_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.aftereach.method.default";
	String DEFAULT_AFTER_ALL_METHOD_TIMEOUT_PROPERTY_NAME = "junit.jupiter.execution.timeout.afterall.method.default";
	String TIMEOUT_MODE_PROPERTY_NAME = "junit.jupiter.execution.timeout.mode";

	Optional<String> getRawConfigurationParameter(String key);

	boolean isParallelExecutionEnabled();

	boolean isExtensionAutoDetectionEnabled();

	ExecutionMode getDefaultExecutionMode();

	ExecutionMode getDefaultClassesExecutionMode();

	TestInstance.Lifecycle getDefaultTestInstanceLifecycle();

	Predicate<ExecutionCondition> getExecutionConditionFilter();

	DisplayNameGenerator getDefaultDisplayNameGenerator();

}
