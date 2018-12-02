/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassFilter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * {@code DiscoverySelectorResolver} resolves {@link TestDescriptor TestDescriptors}
 * for containers and tests selected by DiscoverySelectors with the help of the
 * {@code JavaElementsResolver}.
 *
 * <p>This class is the only public entry point into the discovery package.
 *
 * @since 5.0
 * @see JavaElementsResolver
 */
@API(status = INTERNAL, since = "5.0")
public class DiscoverySelectorResolver {

	private static final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();

	public void resolveSelectors(EngineDiscoveryRequest request, JupiterConfiguration configuration,
			TestDescriptor engineDescriptor) {
		ClassFilter classFilter = buildClassFilter(request, isTestClassWithTests);
		resolve(request, configuration, engineDescriptor, classFilter);
		filter(engineDescriptor, classFilter);
		pruneTree(engineDescriptor);
	}

	private void resolve(EngineDiscoveryRequest request, JupiterConfiguration configuration,
			TestDescriptor engineDescriptor, ClassFilter classFilter) {
		JavaElementsResolver javaElementsResolver = createJavaElementsResolver(configuration, engineDescriptor,
			classFilter);

		request.getSelectorsByType(ClasspathRootSelector.class).forEach(javaElementsResolver::resolveClasspathRoot);
		request.getSelectorsByType(ModuleSelector.class).forEach(javaElementsResolver::resolveModule);
		request.getSelectorsByType(PackageSelector.class).forEach(javaElementsResolver::resolvePackage);
		request.getSelectorsByType(ClassSelector.class).forEach(javaElementsResolver::resolveClass);
		request.getSelectorsByType(MethodSelector.class).forEach(javaElementsResolver::resolveMethod);
		request.getSelectorsByType(UniqueIdSelector.class).forEach(javaElementsResolver::resolveUniqueId);
	}

	private void filter(TestDescriptor engineDescriptor, ClassFilter classFilter) {
		new DiscoveryFilterApplier().applyClassNamePredicate(classFilter::match, engineDescriptor);
	}

	private void pruneTree(TestDescriptor rootDescriptor) {
		rootDescriptor.accept(TestDescriptor::prune);
	}

	private JavaElementsResolver createJavaElementsResolver(JupiterConfiguration configuration,
			TestDescriptor engineDescriptor, ClassFilter classFilter) {

		Set<ElementResolver> resolvers = new LinkedHashSet<>();
		resolvers.add(new TestContainerResolver(configuration));
		resolvers.add(new NestedTestsResolver(configuration));
		resolvers.add(new TestMethodResolver(configuration));
		resolvers.add(new TestFactoryMethodResolver(configuration));
		resolvers.add(new TestTemplateMethodResolver(configuration));

		return new JavaElementsResolver(engineDescriptor, configuration, classFilter, resolvers);
	}

}
