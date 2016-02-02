/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.ServiceLoader;
import java.util.function.Consumer;

import org.junit.gen5.api.extension.EngineExtensionPoint;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.descriptor.EngineBasedExtensionContext;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.engine.support.hierarchical.Container;

/**
 * @since 5.0
 */
@API(Internal)
public class JUnit5EngineDescriptor extends EngineDescriptor implements Container<JUnit5EngineExecutionContext> {

	public JUnit5EngineDescriptor(String uniqueId) {
		super(uniqueId, "JUnit 5");
	}

	@Override
	public JUnit5EngineExecutionContext beforeAll(JUnit5EngineExecutionContext context) {

		ExtensionContext extensionContext = context.getExtensionContext();
		callEngineExtensionPoint(extensionPoint -> extensionPoint.beforeEngine(extensionContext));

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(new ExtensionRegistry())
				.withExtensionContext(extensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public JUnit5EngineExecutionContext afterAll(JUnit5EngineExecutionContext context) throws Exception {
		// TODO: make here work with ThrowableCollector  ref: ClassTestDescriptor#afterAll()

		ExtensionContext extensionContext = context.getExtensionContext();
		callEngineExtensionPoint(extensionPoint -> extensionPoint.afterEngine(extensionContext));

		// @formatter:off
		return context.extend()   // TODO: not really need to extend it
				.withExtensionContext(extensionContext)
				.build();
		// @formatter:on
	}

	private void callEngineExtensionPoint(Consumer<EngineExtensionPoint> consumer) {
		Iterable<EngineExtensionPoint> extensionPoints = ServiceLoader.load(EngineExtensionPoint.class,
			ReflectionUtils.getDefaultClassLoader());

		// TODO: log which classes have discovered

		for (EngineExtensionPoint extensionPoint : extensionPoints) {
			consumer.accept(extensionPoint);
		}
	}

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) throws Exception {

		ExtensionContext extensionContext = new EngineBasedExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this);

		// @formatter:off
		return context.extend()
				.withExtensionContext(extensionContext)
				.build();
		// @formatter:on
	}
}
