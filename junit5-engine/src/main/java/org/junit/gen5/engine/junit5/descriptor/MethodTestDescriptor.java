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

import java.lang.reflect.Method;

import org.junit.gen5.api.Name;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class MethodTestDescriptor extends AbstractTestDescriptor {

	private final String displayName;

	private final Method testMethod;

	public MethodTestDescriptor(String uniqueId, Method testMethod) {
		super(uniqueId);
		Preconditions.notNull(testMethod, "testMethod must not be null");

		this.testMethod = testMethod;
		this.displayName = determineDisplayName();
	}

	private String determineDisplayName() {
		// @formatter:off
		String name = AnnotationUtils.findAnnotation(this.testMethod, Name.class)
				.map(Name::value)
				.filter(nameFromNameAnnotation -> !StringUtils.isBlank(nameFromNameAnnotation))
				.orElse(null);

		if (name != null)
			return name;

		return AnnotationUtils.findAnnotation(this.testMethod, Test.class)
				.map(Test::name)
				.filter(nameFromTestAnnotation -> !StringUtils.isBlank(nameFromTestAnnotation))
				.orElse(this.testMethod.getName());
		// @formatter:on
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public Method getTestMethod() {
		return testMethod;
	}

	@Override
	public final boolean isTest() {
		return true;
	}

}
