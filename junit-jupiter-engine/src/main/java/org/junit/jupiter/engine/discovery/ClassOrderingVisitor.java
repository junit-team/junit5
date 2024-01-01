/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;

/**
 * @since 5.8
 */
class ClassOrderingVisitor
		extends AbstractOrderingVisitor<JupiterEngineDescriptor, ClassBasedTestDescriptor, DefaultClassDescriptor> {

	private final JupiterConfiguration configuration;

	ClassOrderingVisitor(JupiterConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void visit(TestDescriptor testDescriptor) {
		ClassOrderer globalClassOrderer = this.configuration.getDefaultTestClassOrderer().orElse(null);
		doWithMatchingDescriptor(JupiterEngineDescriptor.class, testDescriptor,
			descriptor -> orderContainedClasses(descriptor, globalClassOrderer),
			descriptor -> "Failed to order classes");
	}

	private void orderContainedClasses(JupiterEngineDescriptor jupiterEngineDescriptor, ClassOrderer classOrderer) {
		orderChildrenTestDescriptors(//
			jupiterEngineDescriptor, //
			ClassBasedTestDescriptor.class, //
			DefaultClassDescriptor::new, //
			createDescriptorWrapperOrderer(classOrderer));
	}

	@Override
	protected DescriptorWrapperOrderer getDescriptorWrapperOrderer(
			DescriptorWrapperOrderer inheritedDescriptorWrapperOrderer,
			AbstractAnnotatedDescriptorWrapper<?> descriptorWrapper) {

		AnnotatedElement annotatedElement = descriptorWrapper.getAnnotatedElement();
		return AnnotationUtils.findAnnotation(annotatedElement, TestClassOrder.class)//
				.map(TestClassOrder::value)//
				.<ClassOrderer> map(ReflectionUtils::newInstance)//
				.map(this::createDescriptorWrapperOrderer)//
				.orElse(inheritedDescriptorWrapperOrderer);
	}

	private DescriptorWrapperOrderer createDescriptorWrapperOrderer(ClassOrderer classOrderer) {
		Consumer<List<DefaultClassDescriptor>> orderingAction = classOrderer == null ? null : //
				classDescriptors -> classOrderer.orderClasses(
					new DefaultClassOrdererContext(classDescriptors, this.configuration));

		MessageGenerator descriptorsAddedMessageGenerator = number -> String.format(
			"ClassOrderer [%s] added %s ClassDescriptor(s) which will be ignored.", nullSafeToString(classOrderer),
			number);
		MessageGenerator descriptorsRemovedMessageGenerator = number -> String.format(
			"ClassOrderer [%s] removed %s ClassDescriptor(s) which will be retained with arbitrary ordering.",
			nullSafeToString(classOrderer), number);

		return new DescriptorWrapperOrderer(orderingAction, descriptorsAddedMessageGenerator,
			descriptorsRemovedMessageGenerator);
	}

	private static String nullSafeToString(ClassOrderer classOrderer) {
		return (classOrderer != null ? classOrderer.getClass().getName() : "<unknown>");
	}

}
