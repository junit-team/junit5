/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.net.URI;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link URI} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines}
 * can discover tests or containers based on URIs.
 *
 * @since 1.0
 * @see FileSelector
 * @see DirectorySelector
 * @see org.junit.platform.engine.support.descriptor.UriSource
 */
@API(Experimental)
public class UriSelector implements DiscoverySelector {

	private final URI uri;

	UriSelector(URI uri) {
		this.uri = uri;
	}

	/**
	 * Get the selected {@link URI}.
	 */
	public URI getUri() {
		return this.uri;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("uri", this.uri).toString();
	}

}
