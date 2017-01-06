/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.engine.discovery.predicates.IsScannableTestClass;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * A {@code DiscoverySelectorResolver} resolves selectors with the help of the {@code JavaElementResolver} instances.
 * This class is the only public entry point to the discovery package.
 *
 * @since 5.0
 *
 * @see JavaElementsResolver
 *
 */
@API(Experimental)
public class DiscoverySelectorResolver {

	private static final IsScannableTestClass isScannableTestClass = new IsScannableTestClass();

	public void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
		JavaElementsResolver javaElementsResolver = createJavaElementsResolver(engineDescriptor);
		Predicate<String> classNamePredicate = buildClassNamePredicate(request);

		request.getSelectorsByType(ClasspathRootSelector.class).forEach(selector -> {
			findAllClassesInClasspathRoot(selector.getClasspathRoot(), isScannableTestClass,
				classNamePredicate).forEach(javaElementsResolver::resolveClass);
		});
		request.getSelectorsByType(PackageSelector.class).forEach(selector -> {
			findAllClassesInPackage(selector.getPackageName(), isScannableTestClass, classNamePredicate).forEach(
				javaElementsResolver::resolveClass);
		});
		request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
			javaElementsResolver.resolveClass(selector.getJavaClass());
		});
		request.getSelectorsByType(MethodSelector.class).forEach(selector -> {
			javaElementsResolver.resolveMethod(selector.getJavaClass(), selector.getJavaMethod());
		});
		request.getSelectorsByType(UniqueIdSelector.class).forEach(selector -> {
			javaElementsResolver.resolveUniqueId(selector.getUniqueId());
		});
		pruneTree(engineDescriptor);
	}

	private JavaElementsResolver createJavaElementsResolver(TestDescriptor engineDescriptor) {
		Set<ElementResolver> resolvers = new HashSet<>();
		resolvers.add(new TestContainerResolver());
		resolvers.add(new NestedTestsResolver());
		resolvers.add(new TestMethodResolver());
		resolvers.add(new TestFactoryMethodResolver());
		resolvers.add(new TestTemplateMethodResolver());
		return new JavaElementsResolver(engineDescriptor, resolvers);
	}

	private void pruneTree(TestDescriptor root) {
		TestDescriptor.Visitor removeChildrenWithoutTests = (descriptor) -> {
			if (!descriptor.isRoot() && !descriptor.hasTests()) {
				descriptor.removeFromHierarchy();
			}
		};
		root.accept(removeChildrenWithoutTests);
	}

}
