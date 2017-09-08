/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules.adapter;

import static org.junit.platform.commons.meta.API.Status.INTERNAL;

import org.junit.platform.commons.meta.API;

/**
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public interface GenericBeforeAndAfterAdvice {

	default void before() {
	}

	default void handleTestExecutionException(Throwable cause) throws Throwable {
	}

	default void after() {
	}

}
