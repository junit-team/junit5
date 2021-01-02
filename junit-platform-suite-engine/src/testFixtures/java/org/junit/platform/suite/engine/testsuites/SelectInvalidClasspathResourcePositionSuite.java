/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine.testsuites;

import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasspathResource(value = "org.junit.platform.suite.engine.testcases", line = -1)
@SelectClasspathResource(value = "org.junit.platform.suite.engine.testcases", column = 12)
@SelectClasspathResource(value = "org.junit.platform.suite.engine.testcases", line = 42, column = -12)
public class SelectInvalidClasspathResourcePositionSuite {

}
