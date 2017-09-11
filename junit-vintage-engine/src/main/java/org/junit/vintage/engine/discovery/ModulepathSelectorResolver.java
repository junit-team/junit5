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

import static org.junit.platform.commons.util.ModuleUtils.findAllClassesInModulepath;

import java.util.Collection;
import java.util.function.Predicate;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ModulepathSelector;

/**
 * @since 4.12.1
 */
class ModulepathSelectorResolver implements DiscoverySelectorResolver {

	private final Predicate<String> classNamePredicate;

	ModulepathSelectorResolver(Predicate<String> classNamePredicate) {
		this.classNamePredicate = classNamePredicate;
	}

	@Override
	public void resolve(EngineDiscoveryRequest request, Predicate<Class<?>> classFilter, TestClassCollector collector) {
		// @formatter:off
		request.getSelectorsByType(ModulepathSelector.class)
			.stream()
			.map(name -> findAllClassesInModulepath(classFilter, classNamePredicate))
			.flatMap(Collection::stream)
			.forEach(collector::addCompletely);
		// @formatter:on
	}

}
