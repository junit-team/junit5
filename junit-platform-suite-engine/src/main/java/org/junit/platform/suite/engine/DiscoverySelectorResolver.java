/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

/**
 * @since 1.8
 */
final class DiscoverySelectorResolver {

	// @formatter:off
	private static final EngineDiscoveryRequestResolver<SuiteEngineDescriptor> resolver = EngineDiscoveryRequestResolver.<SuiteEngineDescriptor>builder()
			.addClassContainerSelectorResolver(new IsSuiteClass())
			.addSelectorResolver(context -> new ClassSelectorResolver(
					context.getClassNameFilter(),
					context.getEngineDescriptor(),
					context.getDiscoveryRequest().getConfigurationParameters()))
			.build();
	// @formatter:on

	private static void discoverSuites(SuiteEngineDescriptor engineDescriptor) {
		// @formatter:off
		engineDescriptor.getChildren().stream()
				.map(SuiteTestDescriptor.class::cast)
				.forEach(SuiteTestDescriptor::discover);
		// @formatter:on
	}

	void resolveSelectors(EngineDiscoveryRequest request, SuiteEngineDescriptor engineDescriptor) {
		resolver.resolve(request, engineDescriptor);
		discoverSuites(engineDescriptor);
		engineDescriptor.accept(TestDescriptor::prune);
	}

}
