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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.descriptor.NewPackageTestDescriptor;

public class PackageResolver extends JUnit5TestResolver {
	public static NewPackageTestDescriptor descriptorForParentAndName(TestDescriptor parent, String name) {
		return new NewPackageTestDescriptor(String.format("%s/[package:%s]", parent.getUniqueId(), name), name);
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		List<NewPackageTestDescriptor> packageDescriptors = new LinkedList<>();
		if (parent.isRoot()) {
			packageDescriptors.addAll(resolvePackagesFromSelectors(parent, discoveryRequest));
		}
		else if (parent instanceof NewPackageTestDescriptor) {
			String packageName = ((NewPackageTestDescriptor) parent).getPackageName();
			packageDescriptors.addAll(resolveSubpackages(parent, packageName));
		}

		addChildrenAndNotify(parent, packageDescriptors, discoveryRequest);
	}

    private List<NewPackageTestDescriptor> resolvePackagesFromSelectors(TestDescriptor parent,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
		return discoveryRequest.getSelectorsByType(PackageSelector.class).stream()
				.map(PackageSelector::getPackageName)
				.distinct()
				.map(name -> descriptorForParentAndName(parent, name))
				.collect(Collectors.toList());
		// @formatter:on
	}

	private List<NewPackageTestDescriptor> resolveSubpackages(TestDescriptor parent, String packageName) {
		// @formatter:off
		return ReflectionUtils.findAllPackagesInClasspathRoot(packageName).stream()
				.map(Package::getName)
                .map(name -> name.substring(packageName.length() + 1))
                .map(name -> descriptorForParentAndName(parent, name))
				.collect(Collectors.toList());
		// @formatter:on
	}

    @Override
    public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
        if (selector instanceof PackageSelector) {
            String packageName = ((PackageSelector) selector).getPackageName();
            int splitterIndex = packageName.lastIndexOf('.');
            if (splitterIndex == -1) {
                return getTestDescriptor(root, packageName);
            } else {
                String parentPackageName = packageName.substring(0, splitterIndex);
                String currentPackageName = packageName.substring(splitterIndex + 1);

                TestDescriptor parent = fetchBySelector(PackageSelector.forPackageName(parentPackageName), root).orElse(root);
                return getTestDescriptor(parent, currentPackageName);
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
