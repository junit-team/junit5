/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.Objects;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionContext;
import org.junit.gen5.engine.TestExecutor;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
@Data
@EqualsAndHashCode(of = { "uniqueId" })
public class ClassTestGroup implements TestDescriptor, TestExecutor {

	private final TestDescriptor parent;
	private final Class<?> testClass;
	private final String uniqueId;
	private final String displayName;

	public ClassTestGroup(TestDescriptor parent, Class<?> testClass) {
		Preconditions.notNull(parent, "parent must not be null");
		Preconditions.notNull(testClass, "testClass must not be null");

		this.parent = parent;
		this.testClass = testClass;
		this.uniqueId = determineUniqueId();
		this.displayName = determineDisplayName();
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public void execute(TestExecutionContext context) {
		// @formatter:off
		context.getTestDescriptors().stream()
				.filter(testDescriptor -> Objects.nonNull(testDescriptor.getParent()))
				.filter(testDescriptor -> testDescriptor.getParent().equals(this))
				.map(TestExecutionResolver::forDescriptor)
				.forEach(testExecutor -> testExecutor.execute(context));
		// @formatter:on
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	private String determineUniqueId() {
		if (testClass.isMemberClass()) {
			return String.format("%s$%s", getParent().getUniqueId(), testClass.getSimpleName());
		}
		else {
			return String.format("%s:%s", getParent().getUniqueId(), testClass.getCanonicalName());
		}
	}

	private String determineDisplayName() {
		return ReflectionUtils.getAnnotationFrom(testClass, Test.class).map(test -> test.name()).filter(
			name -> !ObjectUtils.isEmpty(name)).orElse(testClass.getSimpleName());
	}
}
