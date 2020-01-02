/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;

/**
 * @since 5.5
 */
class MethodOrderingVisitor implements TestDescriptor.Visitor {

	private static final Logger logger = LoggerFactory.getLogger(MethodOrderingVisitor.class);

	private final JupiterConfiguration configuration;

	MethodOrderingVisitor(JupiterConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void visit(TestDescriptor testDescriptor) {
		if (testDescriptor instanceof ClassBasedTestDescriptor) {
			ClassBasedTestDescriptor classBasedTestDescriptor = (ClassBasedTestDescriptor) testDescriptor;
			try {
				orderContainedMethods(classBasedTestDescriptor, classBasedTestDescriptor.getTestClass());
			}
			catch (Throwable t) {
				BlacklistedExceptions.rethrowIfBlacklisted(t);
				logger.error(t, () -> "Failed to order methods for " + classBasedTestDescriptor.getTestClass());
			}
		}
	}

	/**
	 * @since 5.4
	 */
	private void orderContainedMethods(ClassBasedTestDescriptor classBasedTestDescriptor, Class<?> testClass) {
		findAnnotation(testClass, TestMethodOrder.class)//
				.map(TestMethodOrder::value)//
				.map(ReflectionUtils::newInstance)//
				.ifPresent(methodOrderer -> {

					Set<? extends TestDescriptor> children = classBasedTestDescriptor.getChildren();

					List<TestDescriptor> nonMethodTestDescriptors = children.stream()//
							.filter(testDescriptor -> !(testDescriptor instanceof MethodBasedTestDescriptor))//
							.collect(Collectors.toList());

					List<DefaultMethodDescriptor> methodDescriptors = children.stream()//
							.filter(MethodBasedTestDescriptor.class::isInstance)//
							.map(MethodBasedTestDescriptor.class::cast)//
							.map(DefaultMethodDescriptor::new)//
							.collect(toCollection(ArrayList::new));

					// Make a local copy for later validation
					Set<DefaultMethodDescriptor> originalMethodDescriptors = new LinkedHashSet<>(methodDescriptors);

					methodOrderer.orderMethods(
						new DefaultMethodOrdererContext(methodDescriptors, testClass, this.configuration));

					int difference = methodDescriptors.size() - originalMethodDescriptors.size();

					if (difference > 0) {
						logger.warn(() -> String.format(
							"MethodOrderer [%s] added %s MethodDescriptor(s) for test class [%s] which will be ignored.",
							methodOrderer.getClass().getName(), difference, testClass.getName()));
					}
					else if (difference < 0) {
						logger.warn(() -> String.format(
							"MethodOrderer [%s] removed %s MethodDescriptor(s) for test class [%s] which will be retained with arbitrary ordering.",
							methodOrderer.getClass().getName(), -difference, testClass.getName()));
					}

					Set<TestDescriptor> sortedMethodTestDescriptors = methodDescriptors.stream()//
							.filter(originalMethodDescriptors::contains)//
							.map(DefaultMethodDescriptor::getTestDescriptor)//
							.collect(toCollection(LinkedHashSet::new));

					// Currently no way to removeAll or addAll children at once.
					Stream.concat(sortedMethodTestDescriptors.stream(), nonMethodTestDescriptors.stream())//
							.forEach(classBasedTestDescriptor::removeChild);
					Stream.concat(sortedMethodTestDescriptors.stream(), nonMethodTestDescriptors.stream())//
							.forEach(classBasedTestDescriptor::addChild);

					// Note: MethodOrderer#getDefaultExecutionMode() is guaranteed
					// to be invoked after MethodOrderer#orderMethods().
					methodOrderer.getDefaultExecutionMode()//
							.map(JupiterTestDescriptor::toExecutionMode)//
							.ifPresent(classBasedTestDescriptor::setDefaultChildExecutionMode);
				});
	}

}
