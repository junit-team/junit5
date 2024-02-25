/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

/**
 * {@code DiscoverySelectorResolver} resolves {@link TestDescriptor TestDescriptors}
 * for containers and tests selected by {@link org.junit.platform.engine.DiscoverySelector
 * DiscoverySelectors}, with the help of an {@link EngineDiscoveryRequestResolver}.
 *
 * <p>This is an internal utility which is only {@code public} in order to provide
 * the {@link org.junit.jupiter.engine.JupiterTestEngine JupiterTestEngine} access
 * to the functionality of the {@code discovery} package.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class DiscoverySelectorResolver {

	// @formatter:off
	private static final EngineDiscoveryRequestResolver<JupiterEngineDescriptor> resolver = EngineDiscoveryRequestResolver.<JupiterEngineDescriptor>builder()
			.addClassContainerSelectorResolver(new IsTestClassWithTests())
			.addSelectorResolver(context -> new ClassSelectorResolver(context.getClassNameFilter(), context.getEngineDescriptor().getConfiguration()))
			.addSelectorResolver(context -> new MethodSelectorResolver(context.getEngineDescriptor().getConfiguration()))
			.addTestDescriptorVisitor(context -> new ClassOrderingVisitor(context.getEngineDescriptor().getConfiguration()))
			.addTestDescriptorVisitor(context -> new MethodOrderingVisitor(context.getEngineDescriptor().getConfiguration()))
			.addTestDescriptorVisitor(context -> TestDescriptor::prune)
			.build();
	// @formatter:on

	public void resolveSelectors(EngineDiscoveryRequest request, JupiterEngineDescriptor engineDescriptor) {
		resolver.resolve(request, engineDescriptor);
	}

}
