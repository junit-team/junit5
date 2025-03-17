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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.engine.DiscoveryIssue;

/**
 * {@code DiscoveryIssueReporter} defines the API for reporting
 * {@link DiscoveryIssue DiscoveryIssues}.
 *
 * @since 1.13
 * @see SelectorResolver.Context
 */
@API(status = EXPERIMENTAL, since = "1.13")
public interface DiscoveryIssueReporter {

	/**
	 * Build the supplied {@link DiscoveryIssue.Builder Builder} and report the
	 * resulting {@link DiscoveryIssue}.
	 */
	default void reportIssue(DiscoveryIssue.Builder builder) {
		reportIssue(builder.build());
	}

	/**
	 * Report the supplied {@link DiscoveryIssue}.
	 */
	void reportIssue(DiscoveryIssue issue);

}
