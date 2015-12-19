/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util.org.junit.gen5.meta;

import static de.schauderhaft.degraph.check.JCheck.*;
import static org.hamcrest.Matchers.is;

import org.junit.Assert;
import org.junit.Test;

/**
 * checks agains dependecy circles on package and module level.
 *
 * Modules in that sense are defined by the package name element after org.junit.gen5,
 * so org.junit.gen5.engine.TestEngine belongs in the module engine.
 */
public class DependencyTests {

	@Test
	public void noCycles() {
		// we can't use noJar(), because with gradle the dependencies of other modules are
		// included as jar files in the path.
		Assert.assertThat(classpath() //
		.printTo("dependencies.graphml") //
		.including("org.junit.gen5.**") //
		.withSlicing("module", "org.junit.gen5.(*).**"), //
			is(violationFree()));
	}
}
