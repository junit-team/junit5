/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.junit.platform.commons.meta.API.Status.MAINTAINED;

import org.junit.platform.commons.meta.API;

/**
 * Marker interface for an execution context used by a concrete implementation
 * of {@link HierarchicalTestEngine} and its collaborators.
 *
 * @since 1.0
 * @see HierarchicalTestEngine
 */
@API(status = MAINTAINED, since = "1.0")
public interface EngineExecutionContext {
}
