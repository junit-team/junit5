/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInModule;

import java.util.Collection;

import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ModuleSelector;

/**
 * @since 4.12.1
 */
class ModuleNameSelectorResolver implements DiscoverySelectorResolver {

	@Override
	public void resolve(EngineDiscoveryRequest request, ClassFilter classFilter, TestClassCollector collector) {
		// @formatter:off
		request.getSelectorsByType(ModuleSelector.class)
			.stream()
			.map(ModuleSelector::getModuleName)
			.map(moduleName -> findAllClassesInModule(moduleName, classFilter))
			.flatMap(Collection::stream)
			.forEach(collector::addCompletely);
		// @formatter:on
	}

}
