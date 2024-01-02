/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.api.io.TempDir.DEFAULT_CLEANUP_MODE_PROPERTY_NAME;
import static org.junit.jupiter.api.io.TempDir.DEFAULT_FACTORY_PROPERTY_NAME;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDirFactory;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Caching implementation of the {@link JupiterConfiguration} API.
 *
 * @since 5.4
 */
@API(status = INTERNAL, since = "5.4")
public class CachingJupiterConfiguration implements JupiterConfiguration {

	private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
	private final JupiterConfiguration delegate;

	public CachingJupiterConfiguration(JupiterConfiguration delegate) {
		this.delegate = delegate;
	}

	@Override
	public Optional<String> getRawConfigurationParameter(String key) {
		return delegate.getRawConfigurationParameter(key);
	}

	@Override
	public <T> Optional<T> getRawConfigurationParameter(String key, Function<String, T> transformer) {
		return delegate.getRawConfigurationParameter(key, transformer);
	}

	@Override
	public boolean isParallelExecutionEnabled() {
		return (boolean) cache.computeIfAbsent(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME,
			key -> delegate.isParallelExecutionEnabled());
	}

	@Override
	public boolean isExtensionAutoDetectionEnabled() {
		return (boolean) cache.computeIfAbsent(EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME,
			key -> delegate.isExtensionAutoDetectionEnabled());
	}

	@Override
	public ExecutionMode getDefaultExecutionMode() {
		return (ExecutionMode) cache.computeIfAbsent(DEFAULT_EXECUTION_MODE_PROPERTY_NAME,
			key -> delegate.getDefaultExecutionMode());
	}

	@Override
	public ExecutionMode getDefaultClassesExecutionMode() {
		return (ExecutionMode) cache.computeIfAbsent(DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME,
			key -> delegate.getDefaultClassesExecutionMode());
	}

	@Override
	public TestInstance.Lifecycle getDefaultTestInstanceLifecycle() {
		return (TestInstance.Lifecycle) cache.computeIfAbsent(DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME,
			key -> delegate.getDefaultTestInstanceLifecycle());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Predicate<ExecutionCondition> getExecutionConditionFilter() {
		return (Predicate<ExecutionCondition>) cache.computeIfAbsent(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME,
			key -> delegate.getExecutionConditionFilter());
	}

	@Override
	public DisplayNameGenerator getDefaultDisplayNameGenerator() {
		return (DisplayNameGenerator) cache.computeIfAbsent(DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME,
			key -> delegate.getDefaultDisplayNameGenerator());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<MethodOrderer> getDefaultTestMethodOrderer() {
		return (Optional<MethodOrderer>) cache.computeIfAbsent(DEFAULT_TEST_METHOD_ORDER_PROPERTY_NAME,
			key -> delegate.getDefaultTestMethodOrderer());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<ClassOrderer> getDefaultTestClassOrderer() {
		return (Optional<ClassOrderer>) cache.computeIfAbsent(DEFAULT_TEST_CLASS_ORDER_PROPERTY_NAME,
			key -> delegate.getDefaultTestClassOrderer());
	}

	@Override
	public CleanupMode getDefaultTempDirCleanupMode() {
		return (CleanupMode) cache.computeIfAbsent(DEFAULT_CLEANUP_MODE_PROPERTY_NAME,
			key -> delegate.getDefaultTempDirCleanupMode());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Supplier<TempDirFactory> getDefaultTempDirFactorySupplier() {
		return (Supplier<TempDirFactory>) cache.computeIfAbsent(DEFAULT_FACTORY_PROPERTY_NAME,
			key -> delegate.getDefaultTempDirFactorySupplier());
	}

}
