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

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;

/**
 *
 * @author Tadaya Tsuyukubo
 */
public class EngineBasedExtensionContext extends AbstractExtensionContext {

	public EngineBasedExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener,
			TestDescriptor testDescriptor) {
		super(parent, engineExecutionListener, testDescriptor);
	}

	@Override
	public String getUniqueId() {
		return getTestDescriptor().getUniqueId();
	}

	@Override
	public String getName() {
		return getTestDescriptor().getName();
	}

	@Override
	public String getDisplayName() {
		return getTestDescriptor().getDisplayName();
	}

	@Override
	public Class<?> getTestClass() {
		return null;
	}

	@Override
	public AnnotatedElement getElement() {
		return null;
	}

}
