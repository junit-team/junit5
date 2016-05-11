/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import org.junit.gen5.commons.meta.API;

/**
 * @since 5.0
 */
@FunctionalInterface
@API(Internal)
public interface TestInstanceProvider {

	Object getTestInstance() throws Exception;

}
