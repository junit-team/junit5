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

import java.util.Set;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;

/**
 * {@link TestDescriptor} for tests based on nested (but not static) Java classes.
 *
 * <p>The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name of parent}@{simple class name}</code>.
 *
 * @since 5.0
 */
@API(Internal)
public class NestedClassTestDescriptor extends ClassTestDescriptor {

	/**
	 * Temporary parallel implementation to string-based constructor
	 */
	public NestedClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
		super(uniqueId, testClass);
	}

	public NestedClassTestDescriptor(String uniqueId, Class<?> testClass) {
		super(uniqueId, testClass);
	}

	@Override
	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext context) {
		return () -> {
			Object outerInstance = context.getTestInstanceProvider().getTestInstance();
			return ReflectionUtils.newInstance(getTestClass(), outerInstance);
		};
	}

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> localTags = super.getTags();
		getParent().ifPresent(parentDescriptor -> localTags.addAll(parentDescriptor.getTags()));
		return localTags;
	}

}
