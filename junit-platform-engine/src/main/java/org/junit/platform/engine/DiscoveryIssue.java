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
 * {@code DiscoveryIssue} represents an issue that was encountered during test
 * discovery by a {@link TestEngine}.
 *
 * @since 1.13
 */
@API(status = EXPERIMENTAL, since = "1.13")
public interface DiscoveryIssue {

	/**
	 * Create a new {@code DiscoveryIssue} with the supplied {@link Severity} and
	 * message.
	 *
	 * @see #builder(Severity, String)
	 */
	static DiscoveryIssue create(Severity severity, String message) {
		return builder(severity, message).build();
	}

	/**
	 * Create a new {@link Builder} for creating a {@code DiscoveryIssue} with
	 * the supplied {@link Severity} and message.
	 *
	 * @see Builder
	 * @see #create(Severity, String)
	 */
	static Builder builder(Severity severity, String message) {
		return new DefaultDiscoveryIssue.Builder(severity, message);
	}

	/**
	 * {@return the severity of this issue}
	 */
	Severity severity();

	/**
	 * {@return the message of this issue}
	 */
	String message();

	/**
	 * {@return the source of this issue}
	 */
	Optional<TestSource> source();

	/**
	 * {@return the cause of this issue}
	 */
	Optional<Throwable> cause();

	/**
	 * The severity of a {@code DiscoveryIssue}.
	 */
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

	/**
	 * Builder for creating a {@code DiscoveryIssue}.
	 */
	interface Builder {

		/**
		 * Set the {@link TestSource} for the {@code DiscoveryIssue}.
		 */
		default Builder source(Optional<TestSource> source) {
			source.ifPresent(this::source);
			return this;
		}

		/**
		 * Set the {@link TestSource} for the {@code DiscoveryIssue}.
		 */
		Builder source(TestSource source);

		/**
		 * Set the {@link Throwable} that caused the {@code DiscoveryIssue}.
		 */
		default Builder cause(Optional<Throwable> cause) {
			cause.ifPresent(this::cause);
			return this;
		}

		/**
		 * Set the {@link Throwable} that caused the {@code DiscoveryIssue}.
		 */
		Builder cause(Throwable cause);

		/**
		 * Build the {@code DiscoveryIssue}.
		 */
		DiscoveryIssue build();

	}
}
