/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for tests based on nested (but not static) Java classes.
 *
 * <h3>Default Display Names</h3>
 *
 * <p>The default display name for a non-static nested test class is the simple
 * name of the class.
 *
 * @since 5.0
 */
@API(Internal)
public class NestedClassTestDescriptor extends ClassTestDescriptor {

	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	public NestedClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
		super(uniqueId, Class::getSimpleName, testClass);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> localTags = super.getTags();
		getParent().ifPresent(parentDescriptor -> localTags.addAll(parentDescriptor.getTags()));
		return localTags;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	protected Object instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionRegistry registry, ExtensionContext extensionContext) {

		// Extensions registered for nested classes and below are not to be used for instantiating outer classes
		Optional<ExtensionRegistry> childExtensionRegistryForOuterInstance = Optional.empty();
		Object outerInstance = parentExecutionContext.getTestInstanceProvider().getTestInstance(
			childExtensionRegistryForOuterInstance);
		Constructor<?> constructor = ReflectionUtils.getDeclaredConstructor(getTestClass());
		return executableInvoker.invoke(constructor, outerInstance, extensionContext, registry);
	}

}
