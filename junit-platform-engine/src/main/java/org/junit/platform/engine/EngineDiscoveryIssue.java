/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import java.util.Optional;

public interface EngineDiscoveryIssue {

	static EngineDiscoveryIssue create(Severity severity, String message) {
		return builder(severity, message).build();
	}

	static Builder builder(Severity severity, String message) {
		return new DefaultEngineDiscoveryIssue.Builder(severity, message);
	}

	Severity severity();

	String message();

	Optional<DiscoverySelector> selector();

	Optional<TestSource> source();

	Optional<Throwable> cause();

	enum Severity {

		/**
		 * Indicates that the engine encountered something that could
		 * be potentially problematic, but could also happen due to a valid
		 * setup or configuration.
		 */
		NOTICE,

		/**
		 * Indicates that a deprecated feature was used that might be
		 * removed or change behavior in a future release.
		 */
		DEPRECATION,

		/**
		 * Indicates that the engine encountered something that is
		 * problematic and might lead to unexpected behavior.
		 */
		WARNING
	}

	interface Builder {

		Builder selector(DiscoverySelector selector);

		default Builder source(Optional<TestSource> source) {
			source.ifPresent(this::source);
			return this;
		}

		Builder source(TestSource source);

		Builder cause(Throwable cause);

		EngineDiscoveryIssue build();

	}
}
