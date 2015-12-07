/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.surefire;

import static org.apache.maven.surefire.report.SimpleReportEntry.ignored;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;

final class RunListenerAdapter implements TestExecutionListener {

	private RunListener runListener;

	public RunListenerAdapter(RunListener reporter) {
		this.runListener = reporter;
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
		runListener.testStarting(createReportEntry(testIdentifier));
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, Throwable t) {
		runListener.testSkipped(createReportEntry(testIdentifier, t));
	}

	@Override
	public void testAborted(TestIdentifier testIdentifier, Throwable t) {
		runListener.testAssumptionFailure(createReportEntry(testIdentifier, t));
	}

	@Override
	public void testFailed(TestIdentifier testIdentifier, Throwable t) {
		runListener.testFailed(createReportEntry(testIdentifier, t));
	}

	@Override
	public void testSucceeded(TestIdentifier testIdentifier) {
		runListener.testSucceeded(createReportEntry(testIdentifier));
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier) {
		return new SimpleReportEntry(getClassNameOrUniqueId(testIdentifier), testIdentifier.getDisplayName());
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier, Throwable throwable) {
		Optional<JavaSource> javaSource = getJavaSource(testIdentifier);
		if (javaSource.isPresent() && javaSource.get().getJavaClass().isPresent()) {
			Class<?> sourceClass = javaSource.get().getJavaClass().get();
			Optional<Method> sourceMethod = javaSource.get().getJavaMethod();
			StackTraceWriter stackTraceWriter = getStackTraceWriter(sourceClass, sourceMethod, throwable);
			return new SimpleReportEntry(getClassNameOrUniqueId(testIdentifier), testIdentifier.getDisplayName(),
				stackTraceWriter, null);
		}
		return ignored(getClassNameOrUniqueId(testIdentifier), testIdentifier.getDisplayName(), throwable.getMessage());
	}

	private StackTraceWriter getStackTraceWriter(Class<?> sourceClass, Optional<Method> sourceMethod,
			Throwable throwable) {
		String className = sourceClass.getName();
		String methodName = sourceMethod.map(Method::getName).orElse("");
		return new PojoStackTraceWriter(className, methodName, throwable);
	}

	private String getClassNameOrUniqueId(TestIdentifier testIdentifier) {
		return getClassName(testIdentifier).orElse(testIdentifier.getUniqueId().toString());
	}

	private Optional<String> getClassName(TestIdentifier testIdentifier) {
		Optional<JavaSource> javaSource = getJavaSource(testIdentifier);
		if (javaSource.isPresent()) {
			if (javaSource.get().getJavaClass().isPresent()) {
				return javaSource.get().getJavaClass().map(Class::getName);
			}
		}
		return Optional.empty();
	}

	private Optional<JavaSource> getJavaSource(TestIdentifier testIdentifier) {
		return testIdentifier.getSource().filter(JavaSource.class::isInstance).map(JavaSource.class::cast);
	}
}
