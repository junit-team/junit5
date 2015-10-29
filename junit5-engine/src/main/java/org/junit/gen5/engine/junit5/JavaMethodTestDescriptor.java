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

import static org.junit.gen5.commons.util.ObjectUtils.nullSafeToString;

import java.lang.reflect.Method;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @author Sam Brannen
 * @since 5.0
 */
@Data
@EqualsAndHashCode
public class JavaMethodTestDescriptor implements TestDescriptor {

	private final String testId;
	private final String displayName;
	private final JavaClassTestDescriptor parent;

	private final Method testMethod;


	public JavaMethodTestDescriptor(Method testMethod, JavaClassTestDescriptor parent) {

		Preconditions.notNull(testMethod, "testMethod must not be null");
		Preconditions.notNull(parent, "parent must not be null");

		this.testMethod = testMethod;
		this.displayName = determineDisplayName();
		this.parent = parent;
		this.testId = createTestId();
	}

	private String createTestId() {
		return String.format("%s#%s(%s)", getParent().getTestId(), testMethod.getName(),
			nullSafeToString(testMethod.getParameterTypes()));
	}

	private String determineDisplayName() {
		Test test = testMethod.getAnnotation(Test.class);
		if (test != null) {
			String customName = test.name();
			if (!ObjectUtils.isEmpty(customName)) {
				return customName;
			}
		}
		return testMethod.getName();

	}

	@Override
	public String getEngineId() {
		return getParent().getEngineId();
	}

	@Override
	public boolean isTest() {
		return true;
	}

}
