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
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionContext;
import org.junit.gen5.engine.TestExecutor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
@Data
@EqualsAndHashCode(of = { "uniqueId" })
public class MethodTest implements TestDescriptor, TestExecutor {

	private final ClassTestGroup parent;
	private final Method testMethod;
	private final String uniqueId;
	private final String displayName;

	public MethodTest(ClassTestGroup parent, Method testMethod) {
		Preconditions.notNull(parent, "parent must not be null");
		Preconditions.notNull(testMethod, "testMethod must not be null");

		this.parent = parent;
		this.testMethod = testMethod;
		this.uniqueId = determineUniqueId();
		this.displayName = determineDisplayName();
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public void execute(TestExecutionContext context) {
		try {
			context.getTestExecutionListener().testStarted(this);
			Class<?> testClass = getParent().getTestClass();

			// TODO Extract test instantiation
			Object testInstance = newInstance(testClass);
			invokeMethod(getTestMethod(), testInstance);
			context.getTestExecutionListener().testSucceeded(this);
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof TestSkippedException) {
				context.getTestExecutionListener().testSkipped(this, targetException);
			}
			else if (targetException instanceof TestAbortedException) {
				context.getTestExecutionListener().testAborted(this, targetException);
			}
			else {
				context.getTestExecutionListener().testFailed(this, targetException);
			}
		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
			throw new IllegalStateException(
				String.format("Test %s is not well-formed and cannot be executed", getUniqueId()), ex);
		}
		catch (Exception ex) {
			context.getTestExecutionListener().testFailed(this, ex);
		}
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	private String determineUniqueId() {
		return String.format("%s#%s(%s)", getParent().getUniqueId(), testMethod.getName(),
			nullSafeToString(testMethod.getParameterTypes()));
	}

	private String determineDisplayName() {
		return ReflectionUtils.getAnnotationFrom(testMethod, Test.class).map(test -> test.name()).filter(
			name -> !ObjectUtils.isEmpty(name)).orElse(testMethod.getName());
	}
}
