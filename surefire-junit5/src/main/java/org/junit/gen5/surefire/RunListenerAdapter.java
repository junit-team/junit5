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

import java.util.Optional;

import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestSource;

final class RunListenerAdapter implements TestExecutionListener {

	private RunListener runListener;

	public RunListenerAdapter(RunListener reporter) {
		this.runListener = reporter;
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		runListener.testStarting(new SimpleReportEntry(getClassName(testDescriptor), testDescriptor.getDisplayName()));
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		StackTraceWriter stackTraceWriter = getStackTraceWriter(testDescriptor, t);
		runListener.testSkipped(ignored(getClassName(testDescriptor), testDescriptor.getDisplayName(), t.getMessage()));
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		StackTraceWriter stackTraceWriter = getStackTraceWriter(testDescriptor, t);
		runListener.testAssumptionFailure(
			ignored(getClassName(testDescriptor), testDescriptor.getDisplayName(), t.getMessage()));
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		StackTraceWriter stackTraceWriter = getStackTraceWriter(testDescriptor, t);
		runListener.testFailed(new SimpleReportEntry(getClassName(testDescriptor), testDescriptor.getDisplayName(),
			stackTraceWriter, null));
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		runListener.testSucceeded(new SimpleReportEntry(getClassName(testDescriptor), testDescriptor.getDisplayName()));
	}

	private StackTraceWriter getStackTraceWriter(TestDescriptor testDescriptor, Throwable t) {
		return new PojoStackTraceWriter(getClassName(testDescriptor), testDescriptor.getDisplayName(), t);
	}

	private String getClassName(TestDescriptor testDescriptor) {
		Optional<TestSource> testSource = testDescriptor.getSource();
		if (testSource.isPresent() && testSource.get() instanceof JavaSource) {
			JavaSource javaSource = (JavaSource) testSource.get();
			if (javaSource.getJavaClass().isPresent()) {
				return javaSource.getJavaClass().get().getName();
			}
		}
		return testDescriptor.getUniqueId();
	}
}
