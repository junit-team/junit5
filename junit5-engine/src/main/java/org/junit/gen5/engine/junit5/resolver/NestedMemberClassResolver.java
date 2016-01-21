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

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;

public class NestedMemberClassResolver extends JUnit5TestResolver {
	private static final Logger LOG = Logger.getLogger(NestedMemberClassResolver.class.getName());

	private Pattern uniqueIdRegExPattern = Pattern.compile("^(.+?):([^$]+\\$[^#]+)$");

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		/*
				Preconditions.notNull(parent, "parent must not be null!");
				Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

				if (parent.isRoot()) {
					List<TestDescriptor> classBasedTestClasses = resolveAllClassesFromSpecification(parent, discoveryRequest);
					getTestResolverRegistry().notifyResolvers(classBasedTestClasses, discoveryRequest);

					List<TestDescriptor> uniqueIdBasedTestClasses = resolveUniqueIdsFromSpecification(parent, discoveryRequest);
					getTestResolverRegistry().notifyResolvers(uniqueIdBasedTestClasses, discoveryRequest);
				}
				else if (parent instanceof ClassTestDescriptor) {
					Class<?> parentClass = ((ClassTestDescriptor) parent).getTestClass();
					List<Class<?>> nestedClasses = ReflectionUtils.findNestedClasses(parentClass,
						nestedClass -> AnnotationUtils.isAnnotated(nestedClass, Nested.class));
					getTestResolverRegistry().notifyResolvers(getTestDescriptorsForTestClasses(parent, nestedClasses),
						discoveryRequest);
				}
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
					if (ReflectionUtils.isNestedClass(testClass)) {
						result.add(getTestDescriptorForTestClass(parent, testClass));
					}
				}
				return result;
			}

			ClassTestDescriptor getTestDescriptorForTestClass(TestDescriptor parentTestDescriptor, Class<?> testClass) {
				ClassTestDescriptor testDescriptor;
				testDescriptor = new ClassTestDescriptor(getTestEngine(), testClass);
				testDescriptor = mergeIntoTree(parentTestDescriptor, testDescriptor);
				return testDescriptor;
			}

			private ClassTestDescriptor mergeIntoTree(TestDescriptor parentTestDescriptor, ClassTestDescriptor testDescriptor) {
				Optional<? extends TestDescriptor> uniqueTestDescriptor = parentTestDescriptor.getRoot().findByUniqueId(
					testDescriptor.getUniqueId());
				if (uniqueTestDescriptor.isPresent()) {
					return (ClassTestDescriptor) uniqueTestDescriptor.get();
				}
				else {
					parentTestDescriptor.addChild(testDescriptor);
					return testDescriptor;
				}
		*/
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		return Optional.empty();
	}
}
