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
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;

final class RunListenerAdapter implements TestExecutionListener {

	private RunListener runListener;

	public RunListenerAdapter(RunListener reporter) {
		this.runListener = reporter;
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		runListener.testStarting(createReportEntry(testDescriptor));
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		runListener.testSkipped(createReportEntry(testDescriptor, t));
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		runListener.testAssumptionFailure(createReportEntry(testDescriptor, t));
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		runListener.testFailed(createReportEntry(testDescriptor, t));
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		runListener.testSucceeded(createReportEntry(testDescriptor));
	}

	private SimpleReportEntry createReportEntry(TestDescriptor testDescriptor) {
		return new SimpleReportEntry(getClassNameOrUniqueId(testDescriptor), testDescriptor.getDisplayName());
	}

	private SimpleReportEntry createReportEntry(TestDescriptor testDescriptor, Throwable throwable) {
		Optional<JavaSource> javaSource = getJavaSource(testDescriptor);
		if (javaSource.isPresent() && javaSource.get().getJavaClass().isPresent()) {
			Class<?> sourceClass = javaSource.get().getJavaClass().get();
			Optional<Method> sourceMethod = javaSource.get().getJavaMethod();
			StackTraceWriter stackTraceWriter = getStackTraceWriter(sourceClass, sourceMethod, throwable);
			return new SimpleReportEntry(getClassNameOrUniqueId(testDescriptor), testDescriptor.getDisplayName(),
				stackTraceWriter, null);
		}
		return ignored(getClassNameOrUniqueId(testDescriptor), testDescriptor.getDisplayName(), throwable.getMessage());
	}

	private StackTraceWriter getStackTraceWriter(Class<?> sourceClass, Optional<Method> sourceMethod,
			Throwable throwable) {
		String className = sourceClass.getName();
		String methodName = sourceMethod.map(Method::getName).orElse("");
		return new PojoStackTraceWriter(className, methodName, throwable);
	}

	private String getClassNameOrUniqueId(TestDescriptor testDescriptor) {
		return getClassName(testDescriptor).orElse(testDescriptor.getUniqueId());
	}

	private Optional<String> getClassName(TestDescriptor testDescriptor) {
		Optional<JavaSource> javaSource = getJavaSource(testDescriptor);
		if (javaSource.isPresent()) {
			if (javaSource.get().getJavaClass().isPresent()) {
				return javaSource.get().getJavaClass().map(Class::getName);
			}
		}
		return Optional.empty();
	}

	private Optional<JavaSource> getJavaSource(TestDescriptor testDescriptor) {
		return testDescriptor.getSource().filter(JavaSource.class::isInstance).map(JavaSource.class::cast);
	}
}
