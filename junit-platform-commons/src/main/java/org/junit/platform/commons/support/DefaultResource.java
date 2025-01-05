/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.net.URI;
import java.util.Objects;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.11
 */
@API(status = INTERNAL, since = "1.12")
public class DefaultResource implements Resource {

	private final String name;
	private final URI uri;

	public DefaultResource(String name, URI uri) {
		this.name = Preconditions.notNull(name, "name must not be null");
		this.uri = Preconditions.notNull(uri, "uri must not be null");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DefaultResource that = (DefaultResource) o;
		return name.equals(that.name) && uri.equals(that.uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, uri);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this) //
				.append("name", name) //
				.append("uri", uri) //
				.toString();
	}
}
