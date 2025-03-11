/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.function.UnaryOperator.identity;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;

/**
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public interface Validatable {

	void validate(IssueReporter issueReporter);

	interface IssueReporter {

		default void reportIssue(Severity severity, String message) {
			reportIssue(severity, message, identity());
		}

		void reportIssue(Severity severity, String message, UnaryOperator<DiscoveryIssue.Builder> issueBuilder);

	}

}
