/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.discovery;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryIssue;

/**
 * A {@code DiscoveryIssueReporter} that discards all reported issues.
 *
 * @since 1.13
 */
class DiscardingDiscoveryIssueReporter implements DiscoveryIssueReporter {

	static final DiscardingDiscoveryIssueReporter INSTANCE = new DiscardingDiscoveryIssueReporter();

	private DiscardingDiscoveryIssueReporter() {
	}

	@Override
	public void reportIssue(DiscoveryIssue.Builder builder) {
		// discard
	}

	@Override
	public void reportIssue(Supplier<DiscoveryIssue> issue) {
		// discard
	}

	@Override
	public void reportIssue(DiscoveryIssue issue) {
		// discard
	}

	@Override
	public <T> Condition<T> createReportingCondition(Predicate<T> predicate, Function<T, DiscoveryIssue> issueCreator) {
		Preconditions.notNull(predicate, "predicate must not be null");
		return predicate::test;
	}
}
