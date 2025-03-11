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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * @since 1.13
 */
@API(status = EXPERIMENTAL, since = "1.13")
public interface DiscoveryIssue {

	static DiscoveryIssue create(Severity severity, String message) {
		return builder(severity, message).build();
	}

	static Builder builder(Severity severity, String message) {
		return new DefaultDiscoveryIssue.Builder(severity, message);
	}

	Severity severity();

	String message();

	Optional<TestSource> source();

	Optional<Throwable> cause();

	enum Severity {

		/**
		 * Indicates that the engine encountered something that could be
		 * potentially problematic, but could also happen due to a valid setup
		 * or configuration.
		 */
		NOTICE,

		/**
		 * Indicates that a deprecated feature was used that might be removed
		 * or change behavior in a future release.
		 */
		DEPRECATION,

		/**
		 * Indicates that the engine encountered something that is problematic
		 * and might lead to unexpected behavior.
		 */
		WARNING,

		/**
		 * Indicates that the engine encountered something that is definitely
		 * problematic and will lead to unexpected behavior.
		 */
		ERROR
	}

	interface Builder {

		default Builder source(Optional<TestSource> source) {
			source.ifPresent(this::source);
			return this;
		}

		Builder source(TestSource source);

		default Builder cause(Optional<Throwable> cause) {
			cause.ifPresent(this::cause);
			return this;
		}

		Builder cause(Throwable cause);

		DiscoveryIssue build();

	}
}
