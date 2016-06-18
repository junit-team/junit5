/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import org.junit.gen5.commons.meta.API;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestExtensionContext;

/**
 * Functional interface for registering a {@link BeforeEach @BeforeEach} method
 * as a pseudo-extension.
 *
 * @since 5.0
 */
@FunctionalInterface
@API(Internal)
public interface BeforeEachMethodAdapter extends Extension {

	void invokeBeforeEachMethod(TestExtensionContext context) throws Throwable;

}
