/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.engine.TestDescriptor;

public class TestResolverResult {
	private List<TestDescriptor> resolvedTests;
	private boolean proceedResolution;

	public static TestResolverResult empty() {
		return new TestResolverResult(Collections.emptyList(), false);
	}

	public static TestResolverResult proceedResolving(TestDescriptor... resolvedTests) {
		return proceedResolving(Arrays.asList(resolvedTests));
	}

	public static TestResolverResult proceedResolving(List<TestDescriptor> resolvedTests) {
		return new TestResolverResult(resolvedTests, true);
	}

	public static TestResolverResult stopResolving(TestDescriptor... resolvedTests) {
		return stopResolving(Arrays.asList(resolvedTests));
	}

	public static TestResolverResult stopResolving(List<TestDescriptor> resolvedTests) {
		return new TestResolverResult(resolvedTests, false);
	}

	private TestResolverResult(List<TestDescriptor> resolvedTests, boolean proceedResolution) {
		this.resolvedTests = resolvedTests;
		this.proceedResolution = proceedResolution;
	}

	public List<TestDescriptor> getResolvedTests() {
		return resolvedTests;
	}

	public boolean isProceedResolution() {
		return proceedResolution;
	}

	public boolean isEmpty() {
		return resolvedTests.isEmpty();
	}
}