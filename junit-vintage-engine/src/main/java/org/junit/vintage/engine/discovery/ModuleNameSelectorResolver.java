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

import java.util.Collection;
import java.util.function.Predicate;

import org.junit.platform.commons.util.ModuleUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ModuleSelector;

/**
 * @since 4.12.1
 */
class ModuleNameSelectorResolver implements DiscoverySelectorResolver {

	private final Predicate<String> classNamePredicate;

	ModuleNameSelectorResolver(Predicate<String> classNamePredicate) {
		this.classNamePredicate = classNamePredicate;
	}

	@Override
	public void resolve(EngineDiscoveryRequest request, Predicate<Class<?>> classFilter, TestClassCollector collector) {
		// @formatter:off
		request.getSelectorsByType(ModuleSelector.class)
			.stream()
			.map(ModuleSelector::getModuleName)
			.map(name -> ModuleUtils.findAllClassesInModule(name, classFilter, classNamePredicate))
			.flatMap(Collection::stream)
			.forEach(collector::addCompletely);
		// @formatter:on
	}

}
