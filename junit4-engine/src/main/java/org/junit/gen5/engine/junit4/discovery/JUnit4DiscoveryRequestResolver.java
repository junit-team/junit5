/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static org.junit.gen5.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.gen5.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.gen5.engine.junit4.discovery.RunnerTestDescriptorAwareFilter.adapter;
import static org.junit.runner.manipulation.Filter.matchMethodDescription;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.gen5.engine.specification.AllClassFilters;
import org.junit.gen5.engine.specification.ClassSelector;
import org.junit.gen5.engine.specification.ClasspathSelector;
import org.junit.gen5.engine.specification.MethodSelector;
import org.junit.gen5.engine.specification.PackageNameSelector;
import org.junit.gen5.engine.specification.UniqueIdSelector;
import org.junit.runner.Description;

public class JUnit4DiscoveryRequestResolver {

	private final EngineDescriptor engineDescriptor;

	public JUnit4DiscoveryRequestResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolve(DiscoveryRequest discoveryRequest) {

		IsPotentialJUnit4TestClass classTester = new IsPotentialJUnit4TestClass();
		TestClassCollector collector = new TestClassCollector();

		discoveryRequest.getElementsByType(ClasspathSelector.class).forEach(selector -> {
			findAllClassesInClasspathRoot(selector.getClasspathRoot(), classTester).forEach(collector::addCompletely);
		});

		discoveryRequest.getElementsByType(PackageNameSelector.class).forEach(selector -> {
			findAllClassesInPackage(selector.getPackageName(), classTester).forEach(collector::addCompletely);
		});

		discoveryRequest.getElementsByType(ClassSelector.class).forEach(selector -> {
			collector.addCompletely(selector.getTestClass());
		});

		discoveryRequest.getElementsByType(MethodSelector.class).forEach(selector -> {
			Class<?> testClass = selector.getTestClass();
			Method testMethod = selector.getTestMethod();
			Description methodDescription = Description.createTestDescription(testClass, testMethod.getName());
			collector.addFiltered(testClass, adapter(matchMethodDescription(methodDescription)));
		});

		discoveryRequest.getElementsByType(UniqueIdSelector.class).forEach(selector -> {
			String uniqueId = selector.getUniqueId();
			String enginePrefix = engineDescriptor.getEngine().getId() + RunnerTestDescriptor.SEPARATOR;
			if (uniqueId.startsWith(enginePrefix)) {
				String testClassName = determineTestClassName(uniqueId, enginePrefix);
				Optional<Class<?>> testClass = ReflectionUtils.loadClass(testClassName);
				if (testClass.isPresent()) {
					collector.addFiltered(testClass.get(), new UniqueIdFilter(uniqueId));
				}
				else {
					// TODO #40 Log warning
				}
			}
		});

		Set<TestClassRequest> requests = convertToTestClassRequests(discoveryRequest, collector);

		new TestClassRequestResolver(engineDescriptor).populateEngineDescriptorFrom(requests);
	}

	private Set<TestClassRequest> convertToTestClassRequests(DiscoveryRequest request, TestClassCollector collector) {
		// TODO #40 Log classes that are filtered out
		ClassFilter classFilter = new AllClassFilters(request.getEngineFiltersByType(ClassFilter.class));
		return collector.toRequests(classFilter::acceptClass);
	}

	private String determineTestClassName(String uniqueId, String enginePrefix) {
		int endIndex = uniqueId.indexOf(JUnit4TestDescriptor.DEFAULT_SEPARATOR);
		if (endIndex >= 0) {
			return uniqueId.substring(enginePrefix.length(), endIndex);
		}
		return uniqueId.substring(enginePrefix.length());
	}
}
