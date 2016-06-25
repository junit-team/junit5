/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.meta;

import static de.schauderhaft.degraph.check.JCheck.classpath;
import static de.schauderhaft.degraph.check.JCheck.violationFree;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.schauderhaft.degraph.configuration.NamedPattern;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * {@code DependencyTests} check against dependency cycles at the package
 * and module levels.
 *
 * <p><em>Modules</em> are defined by the package name element immediately
 * following the {@code org.junit.platform} base package. For example,
 * {@code org.junit.platform.console.ConsoleLauncher} belongs to the
 * {@code console} module.
 */
@RunWith(JUnitPlatform.class)
public class DependencyTests {

	@Test
	void noCycles() {
		// we can't use noJar(), because with gradle the dependencies of other modules are
		// included as jar files in the path.
		//@formatter:off
		assertThat(
			classpath()
				.printTo("dependencies.graphml")
				.including("org.junit.platform.**")
				.including("org.junit.vintage.**")
				.including("org.junit.jupiter.**")
				.withSlicing("module",
					new NamedPattern("org.junit.vintage.engine.**", "junit-vintage-engine"),
					new NamedPattern("org.junit.jupiter.api.**", "junit-jupiter-api"),
					new NamedPattern("org.junit.jupiter.engine.**", "junit-jupiter-engine"),
					"org.junit.platform.(*).**"),
			is(violationFree()));
		//@formatter:on
	}

}
