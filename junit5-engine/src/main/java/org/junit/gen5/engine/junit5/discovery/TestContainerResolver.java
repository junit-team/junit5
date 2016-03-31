/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

public class TestContainerResolver implements ElementResolver {

	public static final String SEGMENT_TYPE = "class";

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
		if (!(element instanceof Class))
			return Collections.emptySet();

		Class<?> clazz = (Class<?>) element;
		if (!isPotentialCandidate(clazz))
			return Collections.emptySet();

		UniqueId uniqueId = createUniqueId(clazz, parent);
		return Collections.singleton(resolveClass(clazz, uniqueId));
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {

		if (!segment.getType().equals(getSegmentType()))
			return Optional.empty();

		if (!requiredParentType().isInstance(parent))
			return Optional.empty();

		String className = getClassName(parent, segment.getValue());

		Optional<Class<?>> optionalContainerClass = ReflectionUtils.loadClass(className);
		if (!optionalContainerClass.isPresent())
			return Optional.empty();

		Class<?> containerClass = optionalContainerClass.get();
		if (!isPotentialCandidate(containerClass))
			return Optional.empty();

		UniqueId uniqueId = createUniqueId(containerClass, parent);
		return Optional.of(resolveClass(containerClass, uniqueId));
	}

	protected Class<? extends TestDescriptor> requiredParentType() {
		return TestDescriptor.class;
	}

	protected String getClassName(TestDescriptor parent, String segmentValue) {
		return segmentValue;
	}

	protected String getSegmentType() {
		return SEGMENT_TYPE;
	}

	protected String getSegmentValue(Class<?> testClass) {
		return testClass.getName();
	}

	protected boolean isPotentialCandidate(Class<?> element) {
		return new IsPotentialTestContainer().test(element);
	}

	protected UniqueId createUniqueId(Class<?> testClass, TestDescriptor parent) {
		return parent.getUniqueId().append(getSegmentType(), getSegmentValue(testClass));
	}

	protected TestDescriptor resolveClass(Class<?> testClass, UniqueId uniqueId) {
		return new ClassTestDescriptor(uniqueId, testClass);
	}
}
