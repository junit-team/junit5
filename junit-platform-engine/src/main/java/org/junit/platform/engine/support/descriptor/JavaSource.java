/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.TestSource;

/**
 * Marker interface for {@link TestSource TestSources} that are based on
 * elements of the Java language.
 *
 * @since 1.0
 */
@API(Experimental)
public interface JavaSource extends TestSource {
}
