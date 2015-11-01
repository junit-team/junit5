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

import static org.junit.gen5.commons.util.ObjectUtils.nullSafeToString;

import java.lang.reflect.Method;

import lombok.Data;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestDescriptor;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @author Sam Brannen
 * @since 5.0
 */
@Data
public class MethodTestDescriptor extends AbstractTestDescriptor {

	private final String displayName;

	private final ClassTestDescriptor parent;

	private final Method testMethod;

	public MethodTestDescriptor(Method testMethod, ClassTestDescriptor parent) {
		Preconditions.notNull(testMethod, "testMethod must not be null");
		Preconditions.notNull(parent, "parent must not be null");

		this.testMethod = testMethod;
		this.displayName = determineDisplayName();
		this.parent = parent;
	}

	private String determineDisplayName() {
		// @formatter:off
		return AnnotationUtils.findAnnotation(this.testMethod, Test.class)
				.map(Test::name)
				.filter(name -> !StringUtils.isBlank(name))
				.orElse(this.testMethod.getName());
		// @formatter:on
	}

	@Override
	public final String getUniqueId() {
		return String.format("%s#%s(%s)", getParent().getUniqueId(), testMethod.getName(),
			nullSafeToString(testMethod.getParameterTypes()));
	}

	@Override
	public final boolean isTest() {
		return true;
	}

}
