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

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.execution.ExecutableInvoker;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;

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
		super(uniqueId, testClass);
	}

	@Override
	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext parentExecutionContext,
			ExtensionRegistry registry, ExtensionContext extensionContext) {

		return () -> {
			Object outerInstance = parentExecutionContext.getTestInstanceProvider().getTestInstance();
			Constructor<?> constructor = ReflectionUtils.getDeclaredConstructor(getTestClass());
			Object instance = executableInvoker.invoke(constructor, outerInstance, extensionContext, registry);
			invokeTestInstancePostProcessors(instance, registry, extensionContext);
			return instance;
		};
	}

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> localTags = super.getTags();
		getParent().ifPresent(parentDescriptor -> localTags.addAll(parentDescriptor.getTags()));
		return localTags;
	}

	@Override
	protected String generateDefaultDisplayName() {
		return getTestClass().getSimpleName();
	}

}
