/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;

/**
 * A selector defines what a {@link TestEngine} can use to discover tests
 * &mdash; for example, the name of a Java class, the path to a file or
 * directory, etc.
 *
 * @since 1.0
 * @see EngineDiscoveryRequest
 */
@API(Experimental)
public interface DiscoverySelector {
}
