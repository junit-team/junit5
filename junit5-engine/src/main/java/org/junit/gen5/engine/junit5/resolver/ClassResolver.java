/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ReflectionUtils.*;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.ClasspathSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.PackageTestDescriptor;

/**
 * @since 5.0
 */
public class ClassResolver extends JUnit5TestResolver {
	private static final String RESOLVER_ID = "class";

	public static ClassTestDescriptor resolveClass(TestDescriptor parent, Class<?> testClass) {
		String packageName = testClass.getPackage().getName();
		String fullQualifiedClassName = testClass.getName();
		String className = fullQualifiedClassName.substring(packageName.length() + 1);
		return fetchFromTreeOrCreateNew(parent, UniqueId.from(RESOLVER_ID, className),
			(uniqueId -> new ClassTestDescriptor(uniqueId.toString(), testClass)));
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		List<TestDescriptor> classDescriptors = new LinkedList<>();
		if (parent.isRoot()) {
			classDescriptors.addAll(resolveClassesFromSelectors(parent, discoveryRequest));
			classDescriptors.addAll(resolveClassesFromClasspath(parent, discoveryRequest));
		}
		else if (parent instanceof PackageTestDescriptor) {
			String packageName = ((PackageTestDescriptor) parent).getPackageName();
			classDescriptors.addAll(resolveTopLevelClassesInPackage(packageName, parent, discoveryRequest));
		}

		for (TestDescriptor child : classDescriptors) {
			getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
		}
	}

	private List<TestDescriptor> resolveClassesFromSelectors(TestDescriptor root,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        return discoveryRequest.getSelectorsByType(ClassSelector.class).stream()
                .map(classSelector -> fetchBySelector(classSelector, root))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        // @formatter:on
	}

	@Override
	public void resolveUniqueId(TestDescriptor parent, UniqueId uniqueId, EngineDiscoveryRequest discoveryRequest) {
		if (uniqueId.currentKey().equals(RESOLVER_ID) && parent instanceof PackageTestDescriptor) {
			String packageName = ((PackageTestDescriptor) parent).getPackageName();
			String className = uniqueId.currentValue();
			Optional<Class<?>> testClass = ReflectionUtils.loadClass(String.format("%s.%s", packageName, className));

			if (testClass.isPresent()) {
				TestDescriptor next = resolveClass(parent, testClass.get());
				parent.addChild(next);
				getTestResolverRegistry().resolveUniqueId(next, uniqueId.getRemainder(), discoveryRequest);
			}
		}
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		if (selector instanceof ClassSelector) {
			Class<?> testClass = ((ClassSelector) selector).getTestClass();
			if (isTopLevelTestClass(testClass)) {
				PackageSelector packageSelector = forPackageName(testClass.getPackage().getName());
				TestDescriptor parent = getTestResolverRegistry().fetchParent(packageSelector, root);
				return getTestDescriptor(parent, testClass);
			}
		}
		return Optional.empty();
	}

	private List<TestDescriptor> resolveClassesFromClasspath(TestDescriptor root,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        List<File> classPathRoots = discoveryRequest.getSelectorsByType(ClasspathSelector.class).stream()
                .map(ClasspathSelector::getClasspathRoot)
                .filter(File::exists)
                .collect(toList());
        // @formatter:on

		if (classPathRoots.isEmpty()) {
			return emptyList();
		}

		List<TestDescriptor> classTestDescriptors = new LinkedList<>();
		for (Class<?> testClass : findAllClassesInClasspathRoots(classPathRoots, this::isTopLevelTestClass)) {
			PackageSelector packageSelector = forPackageName(testClass.getPackage().getName());
			TestDescriptor parent = getTestResolverRegistry().fetchParent(packageSelector, root);
			getTestDescriptor(parent, testClass).ifPresent(classTestDescriptors::add);
		}
		return classTestDescriptors;
	}

	private List<TestDescriptor> resolveTopLevelClassesInPackage(String packageName, TestDescriptor parent,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        return findAllClassesInPackageOnly(packageName, this::isTopLevelTestClass).stream()
                .map(testClass -> resolveClass(parent, testClass))
                .peek(parent::addChild)
                .collect(toList());
        // @formatter:on
	}

	private boolean isTopLevelTestClass(Class<?> candidate) {
		//please do not collapse into single return
		if (isAbstract(candidate))
			return false;
		if (candidate.isLocalClass())
			return false;
		if (candidate.isAnonymousClass())
			return false;
		return !candidate.isMemberClass() || isStatic(candidate);
	}

	private Optional<TestDescriptor> getTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		TestDescriptor child = resolveClass(parent, testClass);
		parent.addChild(child);
		return Optional.of(child);
	}
}
