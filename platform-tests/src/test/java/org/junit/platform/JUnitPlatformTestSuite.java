/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform;

import org.junit.platform.runner.IncludeClassNamePatterns;
import org.junit.platform.runner.IncludeEngines;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.runner.SelectPackages;
import org.junit.runner.RunWith;

/**
 * Test suite for the JUnit Platform.
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
 * @since 1.0
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("org.junit.platform")
@IncludeClassNamePatterns(".*Tests?")
@IncludeEngines("junit-jupiter")
public class JUnitPlatformTestSuite {
}
