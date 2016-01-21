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

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ReflectionUtils.findAllClassesInPackageOnly;
import static org.junit.gen5.commons.util.ReflectionUtils.isAbstract;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NewPackageTestDescriptor;

public class ClassResolver extends JUnit5TestResolver {
	public static ClassTestDescriptor descriptorForParentAndClass(TestDescriptor parent, Class<?> testClass) {
		return new ClassTestDescriptor(parent.getUniqueId() + "/[class:" + testClass.getSimpleName() + "]", testClass);
	}

	private static final Logger LOG = Logger.getLogger(ClassResolver.class.getName());

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		if (parent.isRoot()) {
			resolveClassesFromSelectors(parent, discoveryRequest);
		}
		else if (parent instanceof NewPackageTestDescriptor) {
			String packageName = ((NewPackageTestDescriptor) parent).getPackageName();
			resolveTopLevelClassesInPackage(packageName, parent, discoveryRequest);
		}

	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		return Optional.empty();
	}

	private void resolveClassesFromSelectors(TestDescriptor root, EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        List<Class<?>> testClasses = discoveryRequest.getSelectorsByType(ClassSelector.class).stream()
                .map(ClassSelector::getTestClass)
                .filter(this::isTopLevelTestClass)
                .collect(Collectors.toList());
        // @formatter:on

		for (Class<?> testClass : testClasses) {
			PackageSelector packageSelector = forPackageName(testClass.getPackage().getName());
			TestDescriptor parent = getTestResolverRegistry().fetchParent(packageSelector, root);
			ClassTestDescriptor child = descriptorForParentAndClass(parent, testClass);
			addChildAndNotify(parent, child, discoveryRequest);
		}
	}

	private void resolveTopLevelClassesInPackage(String packageName, TestDescriptor parent,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        List<ClassTestDescriptor> testClasses = findAllClassesInPackageOnly(packageName, aClass -> true).stream()
                .filter(this::isTopLevelTestClass)
                .map(testClass -> descriptorForParentAndClass(parent, testClass))
                .collect(toList());
        // @formatter:on

		addChildrenAndNotify(parent, testClasses, discoveryRequest);
	}

	private boolean isTopLevelTestClass(Class<?> candidate) {
		//please do not collapse into single return
		if (isAbstract(candidate))
			return false;
		if (candidate.isLocalClass())
			return false;
		if (candidate.isAnonymousClass())
			return false;
		return !candidate.isMemberClass();
	}
}
