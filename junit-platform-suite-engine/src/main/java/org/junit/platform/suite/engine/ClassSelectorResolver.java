/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;
import static org.junit.platform.suite.engine.UniqueIdHelper.removePrefix;
import static org.junit.platform.suite.engine.UniqueIdHelper.uniqueIdOfSegment;

import java.util.Optional;
import java.util.function.Predicate;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

final class ClassSelectorResolver implements SelectorResolver {

	private static final IsSuiteClass isSuiteClass = new IsSuiteClass();

	private final Predicate<String> classNameFilter;
	private final SuiteConfiguration configuration;

	ClassSelectorResolver(Predicate<String> classNameFilter, SuiteConfiguration configuration) {
		this.classNameFilter = classNameFilter;
		this.configuration = configuration;
	}

	@Override
	public Resolution resolve(ClassSelector selector, Context context) {
		Class<?> testClass = selector.getJavaClass();
		if (isSuiteClass.test(testClass)) {
			if (classNameFilter.test(testClass.getName())) {
				// @formatter:off
				return toResolution(context.addToParent(parent -> newSuiteDescriptor(testClass, parent))
						.map(suite -> suite.addDiscoveryRequestFrom(testClass)));
				// @formatter:on
			}
		}
		return unresolved();
	}

	@Override
	public Resolution resolve(UniqueIdSelector selector, Context context) {
		UniqueId uniqueId = selector.getUniqueId();
		// @formatter:off
		return uniqueIdOfSegment(uniqueId, SuiteTestDescriptor.SEGMENT_TYPE)
				.flatMap(suiteId -> ReflectionUtils.tryToLoadClass(suiteId.getLastSegment().getValue())
						.toOptional()
						.filter(isSuiteClass)
						.map(testClass -> toResolution(
								context.addToParent(parent -> newSuiteDescriptor(testClass, parent))
										.map(suite -> removePrefix(uniqueId, suiteId)
												// If there was a unique id remaining it targeted
												// a test in the suite
												.map(suite::addDiscoveryRequestFrom)
												// Otherwise the unique id targeted the suite class
												.orElseGet(() -> suite.addDiscoveryRequestFrom(testClass))))))
				.orElseGet(Resolution::unresolved);
		// @formatter:on
	}

	private Resolution toResolution(Optional<SuiteTestDescriptor> suite) {
		return suite.map(Match::exact).map(Resolution::match).orElseGet(Resolution::unresolved);
	}

	private Optional<SuiteTestDescriptor> newSuiteDescriptor(Class<?> testClass, TestDescriptor parent) {
		// @formatter:off
		return Optional.of(new SuiteTestDescriptor(
				parent.getUniqueId().append(SuiteTestDescriptor.SEGMENT_TYPE, testClass.getName()),
				testClass,
				configuration
		));
		// @formatter:on
	}

}
