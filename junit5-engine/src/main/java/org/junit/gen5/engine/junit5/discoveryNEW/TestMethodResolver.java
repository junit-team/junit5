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

import static java.lang.String.format;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsTestMethod;
import org.junit.gen5.engine.junit5.discovery.MethodFinder;

public class TestMethodResolver implements ElementResolver {

	public static final String SEGMENT_TYPE = "method";

	private static final Logger LOG = Logger.getLogger(TestMethodResolver.class.getName());

	private boolean canResolveElement(AnnotatedElement element, TestDescriptor parent) {
		//Do not collapse
		if (!(element instanceof Method))
			return false;
		if (!(parent instanceof ClassTestDescriptor))
			return false;
		return isTestMethod((Method) element);
	}

	@Override
	public Set<TestDescriptor> resolve(AnnotatedElement element, TestDescriptor parent) {
		if (!(element instanceof Method))
			return Collections.emptySet();

		if (!(parent instanceof ClassTestDescriptor))
			return Collections.emptySet();

		Method testMethod = (Method) element;
		if (!isTestMethod(testMethod)) {
			LOG.info(() -> {
				String methodDescription = testMethod.getDeclaringClass().getName() + "#" + testMethod.getName();
				return format("Method '%s' is not a test method", methodDescription);
			});

			return Collections.emptySet();
		}

		UniqueId uniqueId = createUniqueId(testMethod, parent);
		return Collections.singleton(resolveMethod(testMethod, (ClassTestDescriptor) parent, uniqueId));
	}

	@Override
	public boolean canResolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {
		if (!segment.getType().equals(SEGMENT_TYPE))
			return false;

		if (!(parent instanceof ClassTestDescriptor))
			return false;

		Optional<Method> optionalMethod = findMethod(segment, (ClassTestDescriptor) parent);
		if (!optionalMethod.isPresent())
			return false;

		return isTestMethod(optionalMethod.get());
	}

	@Override
	public TestDescriptor resolve(UniqueId.Segment segment, TestDescriptor parent, UniqueId uniqueId) {
		Optional<Method> optionalMethod = findMethod(segment, (ClassTestDescriptor) parent);
		return resolveMethod(optionalMethod.get(), (ClassTestDescriptor) parent, uniqueId);
	}

	private boolean isTestMethod(Method candidate) {
		return new IsTestMethod().test(candidate);
	}

	private UniqueId createUniqueId(Method testMethod, TestDescriptor parent) {
		return parent.getUniqueId().append(SEGMENT_TYPE, testMethod.getName() + "()");
	}

	private Optional<Method> findMethod(UniqueId.Segment segment, ClassTestDescriptor parent) {
		return new MethodFinder().findMethod(segment.getValue(), parent.getTestClass());
	}

	private TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
			UniqueId uniqueId) {
		return new MethodTestDescriptor(uniqueId, parentClassDescriptor.getTestClass(), testMethod);
	}
}
