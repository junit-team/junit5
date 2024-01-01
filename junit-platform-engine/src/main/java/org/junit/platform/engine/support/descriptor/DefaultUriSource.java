/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import java.net.URI;
import java.util.Objects;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * Default implementation of {@link UriSource}.
 *
 * @since 1.3
 */
class DefaultUriSource implements UriSource {

	private static final long serialVersionUID = 1L;

	private final URI uri;

	DefaultUriSource(URI uri) {
		this.uri = Preconditions.notNull(uri, "URI must not be null");
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DefaultUriSource that = (DefaultUriSource) o;
		return Objects.equals(this.uri, that.uri);
	}

	@Override
	public int hashCode() {
		return this.uri.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("uri", this.uri).toString();
	}

}
