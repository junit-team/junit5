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

import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;

public class ClassResolver extends JUnit5TestResolver {
	private static final Logger LOG = Logger.getLogger(ClassResolver.class.getName());

	private Pattern uniqueIdRegExPattern = Pattern.compile("^(.+?):([^#$]+)$");

	@Override
	public void resolveFor(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		/*
				Preconditions.notNull(parent, "parent must not be null!");
				Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

				if (parent.isRoot()) {
					List<TestDescriptor> packageBasedTestClasses = resolveAllPackagesFromSpecification(parent,
						discoveryRequest);
					getTestResolverRegistry().notifyResolvers(packageBasedTestClasses, discoveryRequest);

					List<TestDescriptor> classBasedTestClasses = resolveAllClassesFromSpecification(parent,
						discoveryRequest);
					getTestResolverRegistry().notifyResolvers(classBasedTestClasses, discoveryRequest);

					List<TestDescriptor> uniqueIdBasedTestClasses = resolveUniqueIdsFromSpecification(parent,
						discoveryRequest);
					getTestResolverRegistry().notifyResolvers(uniqueIdBasedTestClasses, discoveryRequest);
				}
			}

			private List<TestDescriptor> resolveAllPackagesFromSpecification(TestDescriptor parent,
					EngineDiscoveryRequest discoveryRequest) {
				List<TestDescriptor> result = new LinkedList<>();

				for (String packageName : discoveryRequest.getPackages()) {
					List<Class<?>> testClasses = ReflectionUtils.findAllClassesInPackage(packageName, aClass -> true);
					result.addAll(getTestDescriptorsForTestClasses(parent, testClasses));
				}

				return result;
			}

			private List<TestDescriptor> resolveAllClassesFromSpecification(TestDescriptor parent,
					EngineDiscoveryRequest discoveryRequest) {
				List<Class<?>> testClasses = discoveryRequest.getClasses();
				return getTestDescriptorsForTestClasses(parent, testClasses);
			}

			private List<TestDescriptor> resolveUniqueIdsFromSpecification(TestDescriptor parent,
					EngineDiscoveryRequest discoveryRequest) {
				List<String> uniqueIds = discoveryRequest.getUniqueIds();
				List<Class<?>> foundClasses = new LinkedList<>();

				for (String uniqueId : uniqueIds) {
					Matcher matcher = uniqueIdRegExPattern.matcher(uniqueId);
					if (matcher.matches()) {
						try {
							String className = matcher.group(2);
							foundClasses.add(Class.forName(className));
						}
						catch (ClassNotFoundException e) {
							LOG.fine(() -> "Skipping uniqueId " + uniqueId
									+ ": UniqueId does not seem to represent a valid test class.");
						}
					}
				}

				return getTestDescriptorsForTestClasses(parent, foundClasses);
			}

			private List<TestDescriptor> getTestDescriptorsForTestClasses(TestDescriptor parent, List<Class<?>> testClasses) {
				List<TestDescriptor> result = new LinkedList<>();
				for (Class<?> testClass : testClasses) {
					if (!ReflectionUtils.isNestedClass(testClass)) {
						ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(getTestEngine(), testClass);
						parent.addChild(classTestDescriptor);
						result.add(classTestDescriptor);
					}
				}
				return result;
		*/
	}
}
