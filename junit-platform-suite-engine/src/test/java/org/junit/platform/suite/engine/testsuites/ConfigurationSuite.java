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

import static org.junit.jupiter.engine.Constants.DEACTIVATE_ALL_CONDITIONS_PATTERN;
import static org.junit.jupiter.engine.Constants.DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME;

import org.junit.platform.suite.api.Configuration;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.engine.testcases.Conditional;

@Suite
@SelectClasses(Conditional.class)
@Configuration(key = DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME, value = DEACTIVATE_ALL_CONDITIONS_PATTERN)
public class ConfigurationSuite {

}
