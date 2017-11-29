/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.extension.ExtensionRegistry.createRegistryWithDefaultExtensions;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEngineExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEngineExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

/**
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class JupiterEngineDescriptor extends EngineDescriptor implements Node<JupiterEngineExecutionContext> {

	private List<Extension> engineLevelExtensions = new ArrayList<>();

	public JupiterEngineDescriptor(UniqueId uniqueId) {
		super(uniqueId, "JUnit Jupiter");
	}

	public void registerExtension(Extension extension) {
		engineLevelExtensions.add(extension);
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		ExtensionRegistry extensionRegistry = createRegistryWithDefaultExtensions(context.getConfigurationParameters());
		EngineExecutionListener executionListener = context.getExecutionListener();
		ExtensionContext extensionContext = new JupiterEngineExtensionContext(executionListener, this);
		ThrowableCollector throwableCollector = new ThrowableCollector();

		// Register all discovered engine-level extensions.
		engineLevelExtensions.forEach(extension -> extensionRegistry.registerExtension(extension, this));

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(extensionRegistry)
				.withExtensionContext(extensionContext)
                .withThrowableCollector(throwableCollector)
				.build();
		// @formatter:on
	}

	@Override
	public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
		invokeAllBeforeEngineExecutionCallbacks(context);
		return context;
	}

	private void invokeAllBeforeEngineExecutionCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		for (BeforeEngineExecutionCallback callback : registry.getExtensions(BeforeEngineExecutionCallback.class)) {
			throwableCollector.execute(() -> callback.beforeEngineExecution(extensionContext));
			if (throwableCollector.isNotEmpty()) {
				break;
			}
		}
	}

	@Override
	public void after(JupiterEngineExecutionContext context) {
		invokeAllAfterEngineExecutionCallbacks(context);
	}

	private void invokeAllAfterEngineExecutionCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		ExtensionContext.Store store = extensionContext.getStore(AfterEngineExecutionCallback.NAMESPACE);

		registry.getReversedExtensions(AfterEngineExecutionCallback.class) //
				.forEach(extension -> throwableCollector.execute(() -> {
					extension.getTestInstanceClass().ifPresent(key -> extension.setTestInstance(store.get(key)));
					extension.afterEngineExecution(extensionContext);
				}));

		// TODO Handle collected throwable...
		if (throwableCollector.isNotEmpty()) {
			throwableCollector.getThrowable().printStackTrace();
		}
	}
}
