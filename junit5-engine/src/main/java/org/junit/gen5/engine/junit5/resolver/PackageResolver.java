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
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.descriptor.PackageTestDescriptor;

public class PackageResolver extends JUnit5TestResolver {
	public static PackageTestDescriptor descriptorForParentAndName(TestDescriptor parent, String packageName) {
		int index = packageName.lastIndexOf('.');
		String name = index == -1 ? packageName : packageName.substring(index + 1);
		String uniqueId = String.format("%s/[package:%s]", parent.getUniqueId(), name);

		if (parent.findByUniqueId(uniqueId).isPresent()) {
			return (PackageTestDescriptor) parent.findByUniqueId(uniqueId).get();
		}
		else {
			return new PackageTestDescriptor(uniqueId, packageName);
		}
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		List<TestDescriptor> packageDescriptors = new LinkedList<>();
		if (parent.isRoot()) {
			packageDescriptors.addAll(resolvePackagesFromSelectors(parent, discoveryRequest));
		}
		else if (parent instanceof PackageTestDescriptor) {
			String packageName = ((PackageTestDescriptor) parent).getPackageName();
			packageDescriptors.addAll(resolveSubpackages(parent, packageName));
		}

		for (TestDescriptor child : packageDescriptors) {
			getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
		}
	}

	private List<TestDescriptor> resolvePackagesFromSelectors(TestDescriptor root,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        return discoveryRequest.getSelectorsByType(PackageSelector.class).stream()
                .distinct()
                .map(packageSelector -> fetchBySelector(packageSelector, root))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        // @formatter:on
	}

	private List<PackageTestDescriptor> resolveSubpackages(TestDescriptor parent, String packageName) {
		// @formatter:off
		return ReflectionUtils.findAllPackagesInClasspathRoot(packageName).stream()
				.map(Package::getName)
                .map(name -> descriptorForParentAndName(parent, name))
                .peek(parent::addChild)
				.collect(toList());
		// @formatter:on
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		if (selector instanceof PackageSelector) {
			String packageName = ((PackageSelector) selector).getPackageName();
			int splitterIndex = packageName.lastIndexOf('.');
			if (splitterIndex == -1) {
				return getTestDescriptor(root, packageName);
			}
			else {
				String parentPackageName = packageName.substring(0, splitterIndex);
				TestDescriptor parent = fetchBySelector(forPackageName(parentPackageName), root).orElse(root);
				return getTestDescriptor(parent, packageName);
			}
		}
		return Optional.empty();
	}

	private Optional<TestDescriptor> getTestDescriptor(TestDescriptor parent, String packageName) {
		Optional<TestDescriptor> child = Optional.of(descriptorForParentAndName(parent, packageName));
		child.ifPresent(parent::addChild);
		return child;
	}
}
