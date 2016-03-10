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

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @author Tadaya Tsuyukubo
 */
public class GlobalExtensionContext extends AbstractExtensionContext {

	public GlobalExtensionContext(EngineExecutionListener engineExecutionListener, TestDescriptor testDescriptor) {
		super(null, engineExecutionListener, testDescriptor);
	}

	@Override
	public String getUniqueId() {
		return "global";
	}

	@Override
	public String getName() {
		return "global";
	}

	@Override
	public String getDisplayName() {
		return "Global Context";
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
