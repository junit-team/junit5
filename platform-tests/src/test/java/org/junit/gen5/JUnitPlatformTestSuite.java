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

import org.junit.gen5.junit4.runner.IncludeClassNamePattern;
import org.junit.gen5.junit4.runner.IncludeEngines;
import org.junit.gen5.junit4.runner.JUnitPlatform;
import org.junit.gen5.junit4.runner.SelectPackages;
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
 * @since 5.0
 */
@RunWith(JUnitPlatform.class)
//@formatter:off
@SelectPackages({
	"org.junit.gen5.commons",
	"org.junit.gen5.launcher",
	"org.junit.gen5.engine",
	"org.junit.gen5.console",
	"org.junit.gen5.junit4.runner"
})
//@formatter:on
@IncludeClassNamePattern(".*Tests?")
@IncludeEngines("junit5")
public class JUnitPlatformTestSuite {
}
