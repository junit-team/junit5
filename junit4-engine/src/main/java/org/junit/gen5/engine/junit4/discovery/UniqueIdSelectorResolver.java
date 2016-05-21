/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static java.lang.String.format;
import static org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor.ENGINE_ID;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.UniqueIdSelector;

class UniqueIdSelectorResolver extends DiscoverySelectorResolver<UniqueIdSelector> {

	private final Logger logger;

	UniqueIdSelectorResolver(Logger logger) {
		super(UniqueIdSelector.class);
		this.logger = logger;
	}

	@Override
	void resolve(UniqueIdSelector selector, TestClassCollector collector) {
		UniqueId uniqueId = UniqueId.parse(selector.getUniqueId());
		if (UniqueId.forEngine(ENGINE_ID).equals(uniqueId)) {
			logger.warning(() -> format("Unresolvable Unique ID (%s): Cannot resolve the engine's unique ID",
				uniqueId.getUniqueString()));
		}
		else {
			uniqueId.getEngineId().ifPresent(engineId -> {
				if (engineId.equals(ENGINE_ID)) {
					String testClassName = determineTestClassName(uniqueId);
					resolveIntoFilteredTestClass(testClassName, uniqueId, collector);
				}
			});
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

	private String determineTestClassName(UniqueId uniqueId) {
		List<UniqueId.Segment> segments = uniqueId.getSegments();
		segments.remove(0); // drop engine node
		UniqueId.Segment runnerSegment = segments.remove(0);
		//TODO Check that it really is a runner segment
		return runnerSegment.getValue();
	}

}
