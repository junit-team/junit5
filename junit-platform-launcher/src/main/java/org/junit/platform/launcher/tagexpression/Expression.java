/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Collection;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestTag;

/**
 * An expression can be evaluated against a collection of {@link TestTag test tags} to decide if they match the expression.
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public interface Expression {
	boolean evaluate(Collection<TestTag> tags);
}
