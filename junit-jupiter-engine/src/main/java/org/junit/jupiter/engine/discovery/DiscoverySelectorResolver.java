/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.ModuleUtils.findAllClassesInModule;
import static org.junit.platform.commons.util.ModuleUtils.findAllClassesInModulepath;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.discovery.predicates.IsScannableTestClass;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.ModulepathSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * {@code DiscoverySelectorResolver} resolves selectors with the help of a
 * {@code JavaElementsResolver}.
 *
 * <p>This class is the only public entry point into the discovery package.
 *
 * @since 5.0
 * @see JavaElementsResolver
 */
@API(status = INTERNAL, since = "5.0")
public class DiscoverySelectorResolver {

	private static final IsScannableTestClass isScannableTestClass = new IsScannableTestClass();

	public void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
		JavaElementsResolver javaElementsResolver = createJavaElementsResolver(engineDescriptor);
		Predicate<String> classNamePredicate = buildClassNamePredicate(request);

		request.getSelectorsByType(ClasspathRootSelector.class).forEach(selector -> {
			findAllClassesInClasspathRoot(selector.getClasspathRoot(), isScannableTestClass,
				classNamePredicate).forEach(javaElementsResolver::resolveClass);
		});
		request.getSelectorsByType(ModulepathSelector.class).forEach(selector -> {
			findAllClassesInModulepath(isScannableTestClass, classNamePredicate).forEach(
				javaElementsResolver::resolveClass);
		});
		request.getSelectorsByType(ModuleSelector.class).forEach(selector -> {
			findAllClassesInModule(selector.getModuleName(), isScannableTestClass, classNamePredicate).forEach(
				javaElementsResolver::resolveClass);
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

	private void pruneTree(TestDescriptor rootDescriptor) {
		rootDescriptor.accept(TestDescriptor::prune);
	}

	private JavaElementsResolver createJavaElementsResolver(TestDescriptor engineDescriptor) {
		Set<ElementResolver> resolvers = new LinkedHashSet<>();
		resolvers.add(new TestContainerResolver());
		resolvers.add(new NestedTestsResolver());
		resolvers.add(new TestMethodResolver());
		resolvers.add(new TestFactoryMethodResolver());
		resolvers.add(new TestTemplateMethodResolver());
		return new JavaElementsResolver(engineDescriptor, resolvers);
	}

}
