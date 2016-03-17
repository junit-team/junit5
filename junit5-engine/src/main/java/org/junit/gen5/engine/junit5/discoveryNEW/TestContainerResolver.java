/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discoveryNEW;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsPotentialTestContainer;

public class TestContainerResolver implements ElementResolver {

	public static final String SEGMENT_TYPE = "class";

	@Override
	public Set<TestDescriptor> resolve(AnnotatedElement element, TestDescriptor parent) {
		if (!(element instanceof Class))
			return Collections.emptySet();

		Class<?> clazz = (Class<?>) element;
		if (!isPotentialTestContainer(clazz))
			return Collections.emptySet();

		UniqueId uniqueId = createUniqueId(clazz, parent);
		return Collections.singleton(resolveClass(clazz, parent, uniqueId));
	}

	@Override
	public Optional<TestDescriptor> resolve(UniqueId.Segment segment, TestDescriptor parent) {

		if (!segment.getType().equals(SEGMENT_TYPE))
			return Optional.empty();

		Optional<Class<?>> optionalContainerClass = ReflectionUtils.loadClass(segment.getValue());
		if (!optionalContainerClass.isPresent())
			return Optional.empty();

		Class<?> containerClass = optionalContainerClass.get();
		if (!isPotentialTestContainer(containerClass))
			return Optional.empty();

		UniqueId uniqueId = createUniqueId(containerClass, parent);
		return Optional.of(resolveClass(containerClass, parent, uniqueId));
	}

	private boolean isPotentialTestContainer(Class<?> element) {
		return new IsPotentialTestContainer().test(element);
	}

	private UniqueId createUniqueId(Class<?> testClass, TestDescriptor parent) {
		return parent.getUniqueId().append(SEGMENT_TYPE, testClass.getName());
	}

	private TestDescriptor resolveClass(Class<?> testClass, TestDescriptor parent, UniqueId uniqueId) {
		return new ClassTestDescriptor(uniqueId, testClass);
	}
}
