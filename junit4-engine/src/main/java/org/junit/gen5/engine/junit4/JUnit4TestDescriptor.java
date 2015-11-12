/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static java.util.Collections.emptySet;
import static org.junit.gen5.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;
import org.junit.runner.Description;

abstract class JUnit4TestDescriptor extends AbstractTestDescriptor {

	public static String ENGINE_ID = "junit4";

	protected JUnit4TestDescriptor(String uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getDisplayName() {
		return getDescription().getDisplayName();
	}

	@Override
	public boolean isTest() {
		return getDescription().isTest();
	}

	@Override
	public Set<TestTag> getTags() {
		return emptySet();
	}

	public abstract Description getDescription();

	@Override
	public Optional<TestSource> getSource() {
		Optional<Method> testMethod = getTestMethod();
		if (testMethod.isPresent()) {
			return Optional.of(new JavaSource(testMethod.get()));
		}
		return getTestClass().map(JavaSource::new);
	}

	public Optional<Method> getTestMethod() {
		Optional<Class<?>> testClass = getTestClass();
		String methodName = getDescription().getMethodName();
		if (testClass.isPresent() && methodName != null) {
			List<Method> methods = findMethods(testClass.get(), method -> methodName.equals(method.getName()));
			if (methods.size() == 1) {
				return Optional.of(methods.get(0));
			}
		}
		return Optional.empty();
	}

	public Optional<Class<?>> getTestClass() {
		return Optional.ofNullable(getDescription().getTestClass());
	}

}
