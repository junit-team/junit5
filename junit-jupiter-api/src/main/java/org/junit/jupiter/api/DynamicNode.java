/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.net.URI;
import java.util.Optional;

import org.apiguardian.api.API;
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
@API(status = MAINTAINED, since = "5.3")
public abstract class DynamicNode {

	private final String displayName;

	/** Custom test source {@link URI} associated with this node; potentially {@code null}. */
	private final URI testSourceUri;

	DynamicNode(String displayName, URI testSourceUri) {
		this.displayName = Preconditions.notBlank(displayName, "displayName must not be null or blank");
		this.testSourceUri = testSourceUri;
	}

	/**
	 * Get the display name of this {@code DynamicNode}.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Get the custom test source {@link URI} of this {@code DynamicNode}.
	 *
	 * @return an {@code Optional} containing the custom test source {@link URI};
	 * never {@code null} but potentially empty
	 * @since 5.3
	 */
	public Optional<URI> getTestSourceUri() {
		return Optional.ofNullable(testSourceUri);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this) //
				.append("displayName", displayName) //
				.append("testSourceUri", testSourceUri) //
				.toString();
	}

}
