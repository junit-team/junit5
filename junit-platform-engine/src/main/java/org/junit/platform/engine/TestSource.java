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

import static org.junit.platform.commons.meta.API.Status.STABLE;

import java.io.Serializable;

import org.junit.platform.commons.meta.API;

/**
 * Representation of the source of a test or container used to navigate to
 * its location by IDEs and build tools.
 *
 * <p>This is a marker interface. Clients need to check instances for concrete
 * subclasses or subinterfaces.
 *
 * <p>Implementations of this interface need to ensure that they are
 * <em>serializable</em> and <em>immutable</em> since they may be used as data
 * transfer objects.
 *
 * @since 1.0
 */
@API(status = STABLE, since = "1.0")
public interface TestSource extends Serializable {
}
