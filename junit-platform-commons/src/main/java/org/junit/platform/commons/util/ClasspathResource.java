/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.net.URI;
import java.util.Objects;

import org.junit.platform.commons.support.Resource;

/**
 * @since 1.11
 */
class ClasspathResource implements Resource {

	private final String name;
	private final URI uri;

	ClasspathResource(String name, URI uri) {
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
		ClasspathResource that = (ClasspathResource) o;
		return name.equals(that.name) && uri.equals(that.uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, uri);
	}
}
