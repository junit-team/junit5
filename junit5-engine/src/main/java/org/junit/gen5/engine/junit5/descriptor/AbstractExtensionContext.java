/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;

abstract class AbstractExtensionContext implements ExtensionContext {

	protected abstract TestDescriptor getTestDescriptor();

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	private ExtensionContext parent;
	private final JUnit5EngineExecutionContext engineExecutionContext;

	AbstractExtensionContext(ExtensionContext parent, JUnit5EngineExecutionContext engineExecutionContext) {
		this.parent = parent;
		this.engineExecutionContext = engineExecutionContext;
	}

	@Override
	public void publishReportEntry(Map<String, String> entry) {
		engineExecutionContext.publishReportEntry(this.getTestDescriptor(), entry);
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	//TODO: Replace with methods to set and get attributes. Maybe with lifecycle?
	public Map<String, Object> getAttributes() {
		return attributes;
	}

}