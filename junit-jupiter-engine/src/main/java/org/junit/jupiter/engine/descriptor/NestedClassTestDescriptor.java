/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.Collections.emptyList;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.DisplayNameUtils.createDisplayNameSupplierForNestedClass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistrar;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * {@link TestDescriptor} for tests based on nested (but not static) Java classes.
 *
 * <h2>Default Display Names</h2>
 *
 * <p>The default display name for a non-static nested test class is the simple
 * name of the class.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class NestedClassTestDescriptor extends ClassBasedTestDescriptor {

	public static final String SEGMENT_TYPE = "nested-class";

	public NestedClassTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration) {
		super(uniqueId, testClass, createDisplayNameSupplierForNestedClass(testClass, configuration), configuration);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public final Set<TestTag> getTags() {
		// return modifiable copy
		Set<TestTag> allTags = new LinkedHashSet<>(this.tags);
		getParent().ifPresent(parentDescriptor -> allTags.addAll(parentDescriptor.getTags()));
		return allTags;
	}

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		TestDescriptor parent = getParent().orElse(null);
		if (parent instanceof ClassBasedTestDescriptor) {
			ClassBasedTestDescriptor parentClassDescriptor = (ClassBasedTestDescriptor) parent;
			List<Class<?>> result = new ArrayList<>(parentClassDescriptor.getEnclosingTestClasses());
			result.add(parentClassDescriptor.getTestClass());
			return result;
		}
		return emptyList();
	}

	// --- Node ----------------------------------------------------------------

	@Override
	protected TestInstances instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionRegistry registry, ExtensionRegistrar registrar, ExtensionContext extensionContext,
			ThrowableCollector throwableCollector) {

		// Extensions registered for nested classes and below are not to be used for instantiating and initializing outer classes
		ExtensionRegistry extensionRegistryForOuterInstanceCreation = parentExecutionContext.getExtensionRegistry();
		TestInstances outerInstances = parentExecutionContext.getTestInstancesProvider().getTestInstances(
			extensionRegistryForOuterInstanceCreation, registrar, throwableCollector);
		return instantiateTestClass(Optional.of(outerInstances), registry, extensionContext);
	}

}
