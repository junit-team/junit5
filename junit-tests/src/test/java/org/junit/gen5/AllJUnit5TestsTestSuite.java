/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5;

import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.junit4.runner.FilterClassName;
import org.junit.gen5.junit4.runner.IncludeEngines;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.gen5.junit4.runner.Packages;
import org.junit.runner.RunWith;

/**
 * Test suite for all JUnit 5 tests.
 *
 * <h3>Logging Configuration</h3>
 *
 * <p>In order for our log4j2 configuration to be used in an IDE, you must
 * set the following system property before running any tests &mdash; for
 * example, in <em>Run Configurations</em> in Eclipse.
 *
 * <pre style="code">
 * -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager
 * </pre>
 *
 * @since 5.0
 */
@RunWith(JUnit5.class)
@Packages("org.junit.gen5")
@FilterClassName(".*Tests?")
@IncludeEngines(JUnit5TestEngine.ENGINE_ID)
public class AllJUnit5TestsTestSuite {
}
