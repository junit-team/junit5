/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.lang.reflect.AnnotatedElement;

import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext;

final class ClassBasedContainerExtensionContext extends AbstractExtensionContext implements ContainerExtensionContext {

	private final ClassTestDescriptor testDescriptor;

	public ClassBasedContainerExtensionContext(ExtensionContext parent, ClassTestDescriptor testDescriptor) {
		super(parent);
		this.testDescriptor = testDescriptor;
	}

	@Override
	public Class<?> getTestClass() {
		return testDescriptor.getTestClass();
	}

	@Override
	public String getDisplayName() {
		return testDescriptor.getDisplayName();
	}

	@Override
	public AnnotatedElement getElement() {
		return getTestClass();
	}

}
