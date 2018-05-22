/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.annotation;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.Serializable;

import org.apiguardian.api.API;

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
 * @since 1.2
 */
@API(status = EXPERIMENTAL, since = "1.2")
public interface TestSource extends Serializable {
}
