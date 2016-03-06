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
import static org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor.DEFAULT_SEPARATOR;
import static org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor.ENGINE_ID;

import java.util.Optional;
import java.util.logging.Logger;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;

class UniqueIdSelectorResolver extends DiscoverySelectorResolver<UniqueIdSelector> {

	private static final String ENGINE_PREFIX = UniqueId.forEngine(ENGINE_ID).getUniqueString()
			+ RunnerTestDescriptor.SEPARATOR;
	private final Logger logger;

	UniqueIdSelectorResolver(Logger logger) {
		super(UniqueIdSelector.class);
		this.logger = logger;
	}

	@Override
	void resolve(UniqueIdSelector selector, TestClassCollector collector) {
		String uniqueId = selector.getUniqueId();
		if (UniqueId.forEngine(ENGINE_ID).getUniqueString().equals(uniqueId)) {
			logger.warning(
				() -> format("Unresolvable Unique ID (%s): Cannot resolve the engine's unique ID", uniqueId));
		}
		else if (uniqueId.startsWith(ENGINE_PREFIX)) {
			String testClassName = determineTestClassName(uniqueId, ENGINE_PREFIX);
			resolveIntoFilteredTestClass(testClassName, uniqueId, collector);
		}
	}

	private void resolveIntoFilteredTestClass(String testClassName, String uniqueId, TestClassCollector collector) {
		Optional<Class<?>> testClass = ReflectionUtils.loadClass(testClassName);
		if (testClass.isPresent()) {
			collector.addFiltered(testClass.get(), new UniqueIdFilter(uniqueId));
		}
		else {
			logger.warning(() -> format("Unresolvable Unique ID (%s): Unknown class %s", uniqueId, testClassName));
		}
	}

	private String determineTestClassName(String uniqueId, String enginePrefix) {
		int endIndex = uniqueId.indexOf(DEFAULT_SEPARATOR);
		if (endIndex >= 0) {
			return uniqueId.substring(enginePrefix.length(), endIndex);
		}
		return uniqueId.substring(enginePrefix.length());
	}

}
