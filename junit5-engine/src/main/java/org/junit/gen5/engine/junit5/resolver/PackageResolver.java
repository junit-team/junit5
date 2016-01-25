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

/**
 * @since 5.0
 */
public class PackageResolver extends JUnit5TestResolver {
	private static final String RESOLVER_ID = "package";

	public static PackageTestDescriptor resolvePackage(TestDescriptor parent, String packageName) {
		int index = packageName.lastIndexOf('.');
		String name = index == -1 ? packageName : packageName.substring(index + 1);
		return fetchFromTreeOrCreateNew(parent, UniqueId.from(RESOLVER_ID, name),
			(uniqueId -> new PackageTestDescriptor(uniqueId.toString(), packageName)));
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
		notifyForAll(packageDescriptors, discoveryRequest);
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

	@Override
	public void resolveUniqueId(TestDescriptor parent, UniqueId uniqueId, EngineDiscoveryRequest discoveryRequest) {
		if (uniqueId.currentKey().equals(RESOLVER_ID)) {
			String packageName = uniqueId.currentValue();
			if (parent instanceof PackageTestDescriptor) {
				PackageTestDescriptor packageTestDescriptor = (PackageTestDescriptor) parent;
				packageName = String.format("%s.%s", packageTestDescriptor.getPackageName(), packageName);
			}

			TestDescriptor next = resolvePackage(parent, packageName);
			parent.addChild(next);
			getTestResolverRegistry().resolveUniqueId(next, uniqueId.getRemainder(), discoveryRequest);
		}
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

	private List<TestDescriptor> resolveSubpackages(TestDescriptor parent, String packageName) {
		// @formatter:off
        return ReflectionUtils.findAllPackagesInClasspathRoot(packageName).stream()
                .map(name -> getTestDescriptor(parent, name))
                .map(Optional::get)
                .collect(toList());
        // @formatter:on
	}

	private Optional<TestDescriptor> getTestDescriptor(TestDescriptor parent, String packageName) {
		TestDescriptor child = resolvePackage(parent, packageName);
		parent.addChild(child);
		return Optional.of(child);
	}
}
