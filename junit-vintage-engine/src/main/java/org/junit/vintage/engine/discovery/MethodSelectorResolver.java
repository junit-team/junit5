/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static org.junit.runner.manipulation.Filter.matchMethodDescription;
import static org.junit.vintage.engine.discovery.RunnerTestDescriptorAwareFilter.adapter;

import java.lang.reflect.Method;

import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.runner.Description;

/**
 * @since 4.12
 */
class MethodSelectorResolver extends DiscoverySelectorResolver<MethodSelector> {

	MethodSelectorResolver() {
		super(MethodSelector.class);
	}

	@Override
	void resolve(MethodSelector selector, TestClassCollector collector) {
		Class<?> testClass = selector.getJavaClass();
		Method testMethod = selector.getJavaMethod();
		Description methodDescription = Description.createTestDescription(testClass, testMethod.getName());
		collector.addFiltered(testClass, adapter(matchMethodDescription(methodDescription)));
	}

}
