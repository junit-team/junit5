/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.time.Instant;

import org.junit.platform.commons.meta.API;

/**
 * Dynamic runtime information.
 *
 * @since 5.0
 */
@API(Experimental)
public interface DynamicRuntime {

	Instant getInstantOfTestFactoryStart();

	boolean wasLastExecutableSuccessful();
}
