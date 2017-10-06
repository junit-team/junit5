/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;

import java.util.Collection;

import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.PackageSelector;

/**
 * @since 4.12
 */
class PackageNameSelectorResolver implements DiscoverySelectorResolver {

	@Override
	public void resolve(EngineDiscoveryRequest request, ClassFilter classFilter, TestClassCollector collector) {
		// @formatter:off
		request.getSelectorsByType(PackageSelector.class)
			.stream()
			.map(PackageSelector::getPackageName)
			.map(packageName -> findAllClassesInPackage(packageName, classFilter))
			.flatMap(Collection::stream)
			.forEach(collector::addCompletely);
		// @formatter:on
	}

}
