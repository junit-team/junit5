/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.apiguardian.api.API.Status.STABLE;

import java.net.URI;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestSource;

/**
 * A {@link TestSource} that can be represented as a {@link URI}.
 *
 * @since 1.0
 * @see org.junit.platform.engine.discovery.UriSelector
 */
@API(status = STABLE, since = "1.0")
public interface UriSource extends TestSource {

	/**
	 * Get the {@link URI} that represents this source.
	 *
	 * @return the source {@code URI}; never {@code null}
	 */
	URI getUri();

}
