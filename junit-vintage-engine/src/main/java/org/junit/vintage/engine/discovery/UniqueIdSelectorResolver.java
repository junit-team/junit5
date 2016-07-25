/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.lang.String.format;
import static java.util.function.Predicate.isEqual;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_RUNNER;

import java.util.Optional;
import java.util.logging.Logger;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * @since 4.12
 */
class UniqueIdSelectorResolver extends DiscoverySelectorResolver<UniqueIdSelector> {

	private final Logger logger;

	UniqueIdSelectorResolver(Logger logger) {
		super(UniqueIdSelector.class);
		this.logger = logger;
	}

	@Override
	void resolve(UniqueIdSelector selector, TestClassCollector collector) {
		UniqueId uniqueId = selector.getUniqueId();
		if (UniqueId.forEngine(ENGINE_ID).equals(uniqueId)) {
			logger.warning(
				() -> format("Unresolvable Unique ID (%s): Cannot resolve the engine's unique ID", uniqueId));
		}
		else {
			uniqueId.getEngineId().filter(isEqual(ENGINE_ID)).ifPresent(
				engineId -> determineTestClassName(uniqueId).ifPresent(
					testClassName -> resolveIntoFilteredTestClass(testClassName, uniqueId, collector)));
		}
	}

	private void resolveIntoFilteredTestClass(String testClassName, UniqueId uniqueId, TestClassCollector collector) {
		Optional<Class<?>> testClass = ReflectionUtils.loadClass(testClassName);
		if (testClass.isPresent()) {
			collector.addFiltered(testClass.get(), new UniqueIdFilter(uniqueId));
		}
		else {
			logger.warning(() -> format("Unresolvable Unique ID (%s): Unknown class %s", uniqueId, testClassName));
		}
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
