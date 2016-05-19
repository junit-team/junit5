/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.net.URI;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.TestSource;

/**
 * @since 5.0
 */
@API(Experimental)
public interface UriSource extends TestSource {

	URI getUri();
}
