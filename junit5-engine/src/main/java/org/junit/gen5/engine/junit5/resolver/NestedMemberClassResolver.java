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

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ReflectionUtils.findNestedClasses;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsNestedTestClass;

/**
 * @since 5.0
 */
public class NestedMemberClassResolver extends JUnit5TestResolver {
	private static final String RESOLVER_ID = "nestedclass";

	private final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	public static NestedClassTestDescriptor resolveNestedClass(TestDescriptor parent, Class<?> testClass) {
		return fetchFromTreeOrCreateNew(parent, UniqueId.from(RESOLVER_ID, testClass.getSimpleName()),
			(uniqueId) -> new NestedClassTestDescriptor(uniqueId.toString(), testClass));
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		List<TestDescriptor> classDescriptors = new LinkedList<>();
		if (parent.isRoot()) {
			classDescriptors.addAll(resolveClassesFromSelectors(parent, discoveryRequest));
		}
		else if (parent instanceof ClassTestDescriptor) {
			Class<?> parentTestClass = ((ClassTestDescriptor) parent).getTestClass();
			classDescriptors.addAll(resolveNestedClassesInClass(parentTestClass, parent, discoveryRequest));
		}

		for (TestDescriptor child : classDescriptors) {
			getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
		}
	}

	private List<TestDescriptor> resolveClassesFromSelectors(TestDescriptor root,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        return discoveryRequest.getSelectorsByType(ClassSelector.class).stream()
                .map(classSelector -> fetchBySelector(classSelector, root))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        // @formatter:on
	}

	@Override
	public void resolveUniqueId(TestDescriptor parent, UniqueId uniqueId, EngineDiscoveryRequest discoveryRequest) {
		if (uniqueId.currentKey().equals(RESOLVER_ID) && parent instanceof ClassTestDescriptor) {
			String enclosingClassName = ((ClassTestDescriptor) parent).getTestClass().getName();
			String className = uniqueId.currentValue();
			Optional<Class<?>> testClass = ReflectionUtils.loadClass(
				String.format("%s$%s", enclosingClassName, className));

			if (testClass.isPresent()) {
				TestDescriptor next = resolveNestedClass(parent, testClass.get());
				parent.addChild(next);
				getTestResolverRegistry().resolveUniqueId(next, uniqueId.getRemainder(), discoveryRequest);
			}
		}
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		if (selector instanceof ClassSelector) {
			Class<?> testClass = ((ClassSelector) selector).getTestClass();
			if (isNestedTestClass.test(testClass)) {
				ClassSelector classSelector = forClass(testClass.getEnclosingClass());
				TestDescriptor parent = getTestResolverRegistry().fetchParent(classSelector, root);
				return getTestDescriptor(parent, testClass);
			}
		}
		return Optional.empty();
	}

	private List<TestDescriptor> resolveNestedClassesInClass(Class<?> parentClass, TestDescriptor parent,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        return findNestedClasses(parentClass, isNestedTestClass::test).stream()
                .map(testClass -> resolveNestedClass(parent, testClass))
                .peek(parent::addChild)
                .collect(toList());
        // @formatter:on
	}

	private Optional<TestDescriptor> getTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		TestDescriptor child = resolveNestedClass(parent, testClass);
		parent.addChild(child);
		return Optional.of(child);
	}
}
