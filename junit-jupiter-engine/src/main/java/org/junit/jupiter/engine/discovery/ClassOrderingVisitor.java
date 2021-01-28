/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.platform.engine.TestDescriptor;

/**
 * @since 5.8
 */
class ClassOrderingVisitor extends AbstractOrderingVisitor implements TestDescriptor.Visitor {

	private final JupiterConfiguration configuration;

	public ClassOrderingVisitor(JupiterConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void visit(TestDescriptor testDescriptor) {
		this.configuration.getDefaultTestClassOrderer().ifPresent(testClassOrderer -> {
			doWithMatchingDescriptor(JupiterEngineDescriptor.class, testDescriptor,
				descriptor -> orderContainedClasses(descriptor, testClassOrderer),
				descriptor -> "Failed to order classes");
		});
	}

	private void orderContainedClasses(JupiterEngineDescriptor jupiterEngineDescriptor, ClassOrderer classOrderer) {
		orderChildrenTestDescriptors(jupiterEngineDescriptor, ClassBasedTestDescriptor.class,
			DefaultClassDescriptor::new,
			descriptorWrappers -> classOrderer.orderClasses(
				new DefaultClassOrdererContext(descriptorWrappers, this.configuration)),
			difference -> String.format("ClassOrderer [%s] added %s ClassDescriptor(s) which will be ignored.",
				classOrderer.getClass().getName(), difference),
			difference -> String.format(
				"ClassOrderer [%s] removed %s ClassDescriptor(s) which will be retained with arbitrary ordering.",
				classOrderer.getClass().getName(), -difference));
	}

}
