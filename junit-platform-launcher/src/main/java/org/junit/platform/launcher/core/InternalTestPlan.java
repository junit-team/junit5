/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.4
 */
class InternalTestPlan extends TestPlan {

	private static final Logger logger = LoggerFactory.getLogger(InternalTestPlan.class);

	private final AtomicBoolean warningEmitted = new AtomicBoolean(false);
	private final LauncherDiscoveryResult discoveryResult;
	private final TestPlan delegate;

	static InternalTestPlan from(LauncherDiscoveryResult discoveryResult) {
		TestPlan delegate = TestPlan.from(discoveryResult.getEngineTestDescriptors());
		return new InternalTestPlan(discoveryResult, delegate);
	}

	private InternalTestPlan(LauncherDiscoveryResult discoveryResult, TestPlan delegate) {
		super(delegate.containsTests());
		this.discoveryResult = discoveryResult;
		this.delegate = delegate;
	}

	LauncherDiscoveryResult getDiscoveryResult() {
		return discoveryResult;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void add(TestIdentifier testIdentifier) {
		if (warningEmitted.compareAndSet(false, true)) {
			logger.warn(() -> "Attempt to modify the TestPlan was detected. "
					+ "A future version of the JUnit Platform will ignore this call and eventually even throw an exception. "
					+ "Please contact your IDE/tool vendor and request a fix (see https://github.com/junit-team/junit5/issues/1732 for details).");
		}
		addInternal(testIdentifier);
	}

	@SuppressWarnings("deprecation")
	void addInternal(TestIdentifier testIdentifier) {
		delegate.add(testIdentifier);
	}

	@Override
	public Set<TestIdentifier> getRoots() {
		return delegate.getRoots();
	}

	@Override
	public Optional<TestIdentifier> getParent(TestIdentifier child) {
		return delegate.getParent(child);
	}

	@Override
	public Set<TestIdentifier> getChildren(TestIdentifier parent) {
		return delegate.getChildren(parent);
	}

	@Override
	public Set<TestIdentifier> getChildren(String parentId) {
		return delegate.getChildren(parentId);
	}

	@Override
	public TestIdentifier getTestIdentifier(String uniqueId) throws PreconditionViolationException {
		return delegate.getTestIdentifier(uniqueId);
	}

	@Override
	public long countTestIdentifiers(Predicate<? super TestIdentifier> predicate) {
		return delegate.countTestIdentifiers(predicate);
	}

	@Override
	public Set<TestIdentifier> getDescendants(TestIdentifier parent) {
		return delegate.getDescendants(parent);
	}

	@Override
	public boolean containsTests() {
		return delegate.containsTests();
	}

}
