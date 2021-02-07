/*
 * Copyright 2015-2021 the original author or authors.
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

final class DiscoverySelectorResolver {

	// @formatter:off
	private static final EngineDiscoveryRequestResolver<SuiteEngineDescriptor> resolver = EngineDiscoveryRequestResolver.<SuiteEngineDescriptor>builder()
			.addClassContainerSelectorResolver(new IsSuiteClass())
			.addSelectorResolver(context -> new ClassSelectorResolver(context.getClassNameFilter(), context.getEngineDescriptor()))
			.addTestDescriptorVisitor(context -> TestDescriptor::prune)
			.build();
	// @formatter:on

	private static void discoverSuite(TestDescriptor descriptor) {
		if (descriptor instanceof SuiteTestDescriptor) {
			SuiteTestDescriptor suite = (SuiteTestDescriptor) descriptor;
			suite.discover();
		}
	}

	void resolveSelectors(EngineDiscoveryRequest request, SuiteEngineDescriptor engineDescriptor) {
		resolver.resolve(request, engineDescriptor);
		engineDescriptor.getChildren().forEach(DiscoverySelectorResolver::discoverSuite);
	}

}
