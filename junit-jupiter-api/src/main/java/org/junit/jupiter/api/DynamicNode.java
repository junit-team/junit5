/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.annotation.TestSource;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@code DynamicNode} serves as the abstract base class for a container or a
 * test case generated at runtime.
 *
 * @since 5.0
 * @see DynamicTest
 * @see DynamicContainer
 */
@API(status = EXPERIMENTAL, since = "5.0")
public abstract class DynamicNode {

	private final String displayName;

	private final TestSource testSource;

	DynamicNode(Builder builder) {
		this.displayName = Preconditions.notBlank(builder.displayName, "displayName must not be null or blank");
		this.testSource = builder.testSource;
	}

	/**
	 * Get the display name of this {@code DynamicNode}.
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Get the optional test source of this {@code DynamicNode}.
	 */
	public Optional<Object> getTestSource() {
		return Optional.ofNullable(testSource);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("displayName", displayName).toString();
	}

	static abstract class Builder {

		String displayName;

		TestSource testSource;

		public Builder setDisplayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder setTestSource(TestSource testSource) {
			this.testSource = testSource;
			return this;
		}

		abstract DynamicNode build();
	}
}
