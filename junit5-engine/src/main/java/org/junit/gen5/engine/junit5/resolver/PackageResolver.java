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
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.descriptor.NewPackageTestDescriptor;

public class PackageResolver extends JUnit5TestResolver {
	public static NewPackageTestDescriptor descriptorForName(String name) {
		return new NewPackageTestDescriptor(String.format("[package:%s]", name), name);
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		List<NewPackageTestDescriptor> packageDescriptors = new LinkedList<>();
		if (parent.isRoot()) {
			packageDescriptors.addAll(resolvePackagesFromSelectors(discoveryRequest));
		}
		else if (parent instanceof NewPackageTestDescriptor) {
			String packageName = ((NewPackageTestDescriptor) parent).getPackageName();
			packageDescriptors.addAll(resolveSubpackages(packageName));
		}

		for (NewPackageTestDescriptor packageDescriptor : packageDescriptors) {
			parent.addChild(packageDescriptor);
			getTestResolverRegistry().notifyResolvers(packageDescriptor, discoveryRequest);
		}
	}

	private List<NewPackageTestDescriptor> resolvePackagesFromSelectors(EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
		return discoveryRequest.getSelectorsByType(PackageSelector.class).stream()
				.map(PackageSelector::getPackageName)
				.distinct()
				.map(PackageResolver::descriptorForName)
				.collect(Collectors.toList());
		// @formatter:on
	}

	private List<NewPackageTestDescriptor> resolveSubpackages(String packageName) {
		// @formatter:off
		return ReflectionUtils.findAllPackagesInClasspathRoot(packageName).stream()
				.map(Package::getName)
				.map(PackageResolver::descriptorForName)
				.collect(Collectors.toList());
		// @formatter:on
	}
}
