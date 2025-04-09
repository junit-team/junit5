/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.LruCache;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.Condition;

/**
 * @since 5.8
 */
class ClassOrderingVisitor extends AbstractOrderingVisitor {

	private final LruCache<ClassBasedTestDescriptor, DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor>> ordererCache = new LruCache<>(
		10);
	private final JupiterConfiguration configuration;
	private final DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> globalOrderer;
	private final Condition<ClassBasedTestDescriptor> noOrderAnnotation;

	ClassOrderingVisitor(JupiterConfiguration configuration, DiscoveryIssueReporter issueReporter) {
		super(issueReporter);
		this.configuration = configuration;
		this.globalOrderer = createGlobalOrderer(configuration);
		this.noOrderAnnotation = issueReporter.createReportingCondition(
			testDescriptor -> !isAnnotated(testDescriptor.getTestClass(), Order.class), testDescriptor -> {
				String message = String.format(
					"Ineffective @Order annotation on class '%s'. It will not be applied because ClassOrderer.OrderAnnotation is not in use.",
					testDescriptor.getTestClass().getName());
				return DiscoveryIssue.builder(Severity.INFO, message) //
						.source(ClassSource.from(testDescriptor.getTestClass())) //
						.build();
			});
	}

	@Override
	public void visit(TestDescriptor testDescriptor) {
		doWithMatchingDescriptor(JupiterEngineDescriptor.class, testDescriptor, this::orderTopLevelClasses,
			descriptor -> "Failed to order top-level classes");
		doWithMatchingDescriptor(ClassBasedTestDescriptor.class, testDescriptor, this::orderNestedClasses,
			descriptor -> "Failed to order nested classes for " + descriptor.getTestClass());
	}

	@Override
	protected boolean shouldNonMatchingDescriptorsComeBeforeOrderedOnes() {
		// Non-matching descriptors can only occur when ordering nested classes in which
		// case they contain only local test methods (for @Nested classes) which must be
		// executed before tests in @Nested test classes. So we add the test methods before
		// adding the @Nested test classes.
		return true;
	}

	private void orderTopLevelClasses(JupiterEngineDescriptor engineDescriptor) {
		orderChildrenTestDescriptors(//
			engineDescriptor, //
			ClassBasedTestDescriptor.class, //
			toValidationAction(globalOrderer), //
			DefaultClassDescriptor::new, //
			globalOrderer);
	}

	private void orderNestedClasses(ClassBasedTestDescriptor descriptor) {
		DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> wrapperOrderer = createAndCacheClassLevelOrderer(
			descriptor);
		orderChildrenTestDescriptors(//
			descriptor, //
			ClassBasedTestDescriptor.class, //
			toValidationAction(wrapperOrderer), //
			DefaultClassDescriptor::new, //
			wrapperOrderer);
	}

	private DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> createGlobalOrderer(
			JupiterConfiguration configuration) {
		ClassOrderer classOrderer = configuration.getDefaultTestClassOrderer().orElse(null);
		return classOrderer == null ? DescriptorWrapperOrderer.noop() : createDescriptorWrapperOrderer(classOrderer);
	}

	private DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> createAndCacheClassLevelOrderer(
			ClassBasedTestDescriptor classBasedTestDescriptor) {
		DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> orderer = createClassLevelOrderer(
			classBasedTestDescriptor);
		ordererCache.put(classBasedTestDescriptor, orderer);
		return orderer;
	}

	private DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> createClassLevelOrderer(
			ClassBasedTestDescriptor classBasedTestDescriptor) {
		return AnnotationSupport.findAnnotation(classBasedTestDescriptor.getTestClass(), TestClassOrder.class)//
				.map(TestClassOrder::value)//
				.map(ReflectionSupport::newInstance)//
				.map(this::createDescriptorWrapperOrderer)//
				.orElseGet(() -> {
					Object parent = classBasedTestDescriptor.getParent().orElse(null);
					if (parent instanceof ClassBasedTestDescriptor) {
						ClassBasedTestDescriptor parentClassTestDescriptor = (ClassBasedTestDescriptor) parent;
						DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> cacheEntry = ordererCache.get(
							parentClassTestDescriptor);
						return cacheEntry != null ? cacheEntry : createClassLevelOrderer(parentClassTestDescriptor);
					}
					return globalOrderer;
				});
	}

	private DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> createDescriptorWrapperOrderer(
			ClassOrderer classOrderer) {
		Consumer<List<DefaultClassDescriptor>> orderingAction = classDescriptors -> classOrderer.orderClasses(
			new DefaultClassOrdererContext(classDescriptors, this.configuration));

		MessageGenerator descriptorsAddedMessageGenerator = number -> String.format(
			"ClassOrderer [%s] added %s ClassDescriptor(s) which will be ignored.", classOrderer.getClass().getName(),
			number);
		MessageGenerator descriptorsRemovedMessageGenerator = number -> String.format(
			"ClassOrderer [%s] removed %s ClassDescriptor(s) which will be retained with arbitrary ordering.",
			classOrderer.getClass().getName(), number);

		return new DescriptorWrapperOrderer<>(classOrderer, orderingAction, descriptorsAddedMessageGenerator,
			descriptorsRemovedMessageGenerator);
	}

	private Optional<Consumer<ClassBasedTestDescriptor>> toValidationAction(
			DescriptorWrapperOrderer<ClassOrderer, DefaultClassDescriptor> wrapperOrderer) {

		if (wrapperOrderer.getOrderer() instanceof ClassOrderer.OrderAnnotation) {
			return Optional.empty();
		}
		return Optional.of(noOrderAnnotation::check);
	}

}
