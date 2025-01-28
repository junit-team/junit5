/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.engine.descriptor.DisplayNameUtils.createDisplayNameSupplierForClass;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.ExtensionContextSupplier;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

public class ContainerTemplateTestDescriptor extends ClassBasedTestDescriptor {

	public static final String SEGMENT_TYPE = "container-template";

	public ContainerTemplateTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration) {
		super(uniqueId, testClass, createDisplayNameSupplierForClass(testClass, configuration), configuration);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<TestTag> getTags() {
		// return modifiable copy
		return new LinkedHashSet<>(this.tags);
	}

	@Override
	public boolean mayRegisterTests() {
		return true;
	}

	// --- ClassBasedTestDescriptor ---------------------------------------------

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return emptyList();
	}

	@Override
	protected TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionContextSupplier extensionContext, ExtensionRegistry registry,
			JupiterEngineExecutionContext context) {
		return instantiateTestClass(Optional.empty(), registry, extensionContext);
	}

	// --- ResourceLockAware ---------------------------------------------------

	@Override
	public Function<ResourceLocksProvider, Set<ResourceLocksProvider.Lock>> getResourceLocksProviderEvaluator() {
		return provider -> provider.provideForClass(getTestClass());
	}
}
