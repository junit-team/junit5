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
 * @since 1.13
 */
@API(status = EXPERIMENTAL, since = "1.13")
public interface DiscoveryIssueReporter {

	default void reportIssue(DiscoveryIssue.Builder builder) {
		reportIssue(builder.build());
	}

	void reportIssue(DiscoveryIssue issue);

}
