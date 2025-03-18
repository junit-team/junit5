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

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.LruCache;
import org.junit.platform.engine.TestDescriptor;

/**
 * @since 5.8
 */
class ClassOrderingVisitor extends AbstractOrderingVisitor {

	private final LruCache<ClassBasedTestDescriptor, DescriptorWrapperOrderer<DefaultClassDescriptor>> ordererCache = new LruCache<>(
		10);
	private final JupiterConfiguration configuration;
	private final DescriptorWrapperOrderer<DefaultClassDescriptor> globalOrderer;

	ClassOrderingVisitor(JupiterConfiguration configuration) {
		this.configuration = configuration;
		this.globalOrderer = createGlobalOrderer(configuration);
	}

	@Override
	public void visit(TestDescriptor testDescriptor) {
		doWithMatchingDescriptor(JupiterEngineDescriptor.class, testDescriptor, this::orderTopLevelClasses,
			descriptor -> "Failed to order top-level classes");
		doWithMatchingDescriptor(ClassBasedTestDescriptor.class, testDescriptor, this::orderNestedClasses,
			descriptor -> "Failed to order nested classes for " + descriptor.getTestClass());
	}

	private void orderTopLevelClasses(JupiterEngineDescriptor engineDescriptor) {
		orderChildrenTestDescriptors(//
			engineDescriptor, //
			ClassBasedTestDescriptor.class, //
			DefaultClassDescriptor::new, //
			globalOrderer);
	}

	private void orderNestedClasses(ClassBasedTestDescriptor descriptor) {
		orderChildrenTestDescriptors(//
			descriptor, //
			ClassBasedTestDescriptor.class, //
			DefaultClassDescriptor::new, //
			lookupOrCreateClassLevelOrderer(descriptor));
	}

	private DescriptorWrapperOrderer<DefaultClassDescriptor> createGlobalOrderer(JupiterConfiguration configuration) {
		ClassOrderer classOrderer = configuration.getDefaultTestClassOrderer().orElse(null);
		return classOrderer == null ? DescriptorWrapperOrderer.noop() : createDescriptorWrapperOrderer(classOrderer);
	}

	private DescriptorWrapperOrderer<DefaultClassDescriptor> lookupOrCreateClassLevelOrderer(
			ClassBasedTestDescriptor classBasedTestDescriptor) {
		return ordererCache.computeIfAbsent(classBasedTestDescriptor, this::createClassLevelOrderer);
	}

	private DescriptorWrapperOrderer<DefaultClassDescriptor> createClassLevelOrderer(
			ClassBasedTestDescriptor classBasedTestDescriptor) {
		return AnnotationSupport.findAnnotation(classBasedTestDescriptor.getTestClass(), TestClassOrder.class)//
				.map(TestClassOrder::value)//
				.map(ReflectionSupport::newInstance)//
				.map(this::createDescriptorWrapperOrderer)//
				.orElseGet(() -> {
					Object parent = classBasedTestDescriptor.getParent().orElse(null);
					if (parent instanceof ClassBasedTestDescriptor) {
						return lookupOrCreateClassLevelOrderer((ClassBasedTestDescriptor) parent);
					}
					return globalOrderer;
				});
	}

	private DescriptorWrapperOrderer<DefaultClassDescriptor> createDescriptorWrapperOrderer(ClassOrderer classOrderer) {
		Consumer<List<DefaultClassDescriptor>> orderingAction = classDescriptors -> classOrderer.orderClasses(
			new DefaultClassOrdererContext(classDescriptors, this.configuration));

		MessageGenerator descriptorsAddedMessageGenerator = number -> String.format(
			"ClassOrderer [%s] added %s ClassDescriptor(s) which will be ignored.", classOrderer.getClass().getName(),
			number);
		MessageGenerator descriptorsRemovedMessageGenerator = number -> String.format(
			"ClassOrderer [%s] removed %s ClassDescriptor(s) which will be retained with arbitrary ordering.",
			classOrderer.getClass().getName(), number);

		return new DescriptorWrapperOrderer<>(orderingAction, descriptorsAddedMessageGenerator,
			descriptorsRemovedMessageGenerator);
	}

}
