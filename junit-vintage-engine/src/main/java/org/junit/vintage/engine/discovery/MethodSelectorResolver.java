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

import static org.junit.vintage.engine.discovery.RunnerTestDescriptorAwareFilter.adapter;

import java.lang.reflect.Method;

import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * @since 4.12
 */
class MethodSelectorResolver implements DiscoverySelectorResolver {

	@Override
	public void resolve(EngineDiscoveryRequest request, ClassFilter classFilter, TestClassCollector collector) {
		request.getSelectorsByType(MethodSelector.class).forEach(selector -> resolve(selector, classFilter, collector));
	}

	private void resolve(MethodSelector selector, ClassFilter classFilter, TestClassCollector collector) {
		Class<?> testClass = selector.getJavaClass();
		if (classFilter.test(testClass)) {
			Method testMethod = selector.getJavaMethod();
			Description methodDescription = Description.createTestDescription(testClass, testMethod.getName());
			collector.addFiltered(testClass, adapter(matchMethodDescription(methodDescription)));
		}
	}

	/**
	 * The method {@link Filter#matchMethodDescription(Description)} returns a
	 * filter that does not account for the case when the description is for a
	 * {@link org.junit.runners.Parameterized} runner.
	 */
	private static Filter matchMethodDescription(final Description desiredDescription) {
		return new Filter() {

			@Override
			public boolean shouldRun(Description description) {
				if (description.isTest()) {
					return desiredDescription.equals(description) || isParameterizedMethod(description);
				}

				// explicitly check if any children want to run
				for (Description each : description.getChildren()) {
					if (shouldRun(each)) {
						return true;
					}
				}
				return false;
			}

			private boolean isParameterizedMethod(Description description) {
				return description.getMethodName().startsWith(desiredDescription.getMethodName() + "[");
			}

			@Override
			public String describe() {
				return String.format("Method %s", desiredDescription.getDisplayName());
			}
		};
	}

}
