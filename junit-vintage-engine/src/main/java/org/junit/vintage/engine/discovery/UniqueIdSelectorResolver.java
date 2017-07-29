/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.lang.String.format;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_RUNNER;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * @since 4.12
 */
class UniqueIdSelectorResolver implements DiscoverySelectorResolver {

	private final Logger logger;

	UniqueIdSelectorResolver(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void resolve(EngineDiscoveryRequest request, Predicate<Class<?>> classFilter, TestClassCollector collector) {
		// @formatter:off
		request.getSelectorsByType(UniqueIdSelector.class)
			.stream()
			.map(UniqueIdSelector::getUniqueId)
			.filter(this::isNotEngineId)
			.filter(this::isForVintageEngine)
			.forEach(uniqueId -> resolveIntoFilteredTestClass(uniqueId, classFilter, collector));
		// @formatter:on
	}

	private boolean isNotEngineId(UniqueId uniqueId) {
		boolean isEngineId = UniqueId.forEngine(ENGINE_ID).equals(uniqueId);
		if (isEngineId) {
			logger.warning(
				() -> format("Unresolvable Unique ID (%s): Cannot resolve the engine's unique ID", uniqueId));
		}
		return !isEngineId;
	}

	private boolean isForVintageEngine(UniqueId uniqueId) {
		// @formatter:off
		return uniqueId.getEngineId()
			.map(engineId -> engineId.equals(ENGINE_ID))
			.orElse(false);
		// @formatter:on
	}

	private void resolveIntoFilteredTestClass(UniqueId uniqueId, Predicate<Class<?>> classFilter,
			TestClassCollector collector) {
		// @formatter:off
		determineTestClassName(uniqueId)
			.flatMap(testClassName -> loadTestClass(testClassName, uniqueId))
			.filter(classFilter)
			.ifPresent(testClass -> collector.addFiltered(testClass, new UniqueIdFilter(uniqueId)));
		// @formatter:on
	}

	private Optional<Class<?>> loadTestClass(String className, UniqueId uniqueId) {
		Optional<Class<?>> testClass = ReflectionUtils.loadClass(className);
		if (!testClass.isPresent()) {
			logger.warning(() -> format("Unresolvable Unique ID (%s): Unknown class %s", uniqueId, className));
		}
		return testClass;
	}

	private Optional<String> determineTestClassName(UniqueId uniqueId) {
		Segment runnerSegment = uniqueId.getSegments().get(1); // skip engine node
		if (SEGMENT_TYPE_RUNNER.equals(runnerSegment.getType())) {
			return Optional.of(runnerSegment.getValue());
		}
		logger.warning(
			() -> format("Unresolvable Unique ID (%s): Unique ID segment after engine segment must be of type \""
					+ SEGMENT_TYPE_RUNNER + "\"",
				uniqueId));
		return Optional.empty();
	}

}
