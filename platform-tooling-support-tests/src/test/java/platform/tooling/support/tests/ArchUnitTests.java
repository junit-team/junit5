/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import org.junit.jupiter.api.Test;
import platform.tooling.support.Helper;

class ArchUnitTests {

	@Test
	void acyclicImportPackages() {
		var packageNames = List.of("org.junit.platform", "org.junit.jupiter", "org.junit.vintage");
		var classes = new ClassFileImporter().importPackages(packageNames);
		// about 431 classes found in classpath of this project
		assertTrue(classes.size() > 300, "expected more than 300 classes, got: " + classes.size());
		acyclic(classes);
	}

	@Test
	void acyclicImportJars() {
		var jarFiles = Helper.loadJarFiles();
		var classes = new ClassFileImporter().importJars(jarFiles);
		// about 928 classes found in all jars
		assertTrue(classes.size() > 800, "expected more than 800 classes, got: " + classes.size());
		acyclic(classes);
	}

	private static void acyclic(JavaClasses classes) {
		slices().matching("org.junit.(*)..").should().beFreeOfCycles().check(classes);
	}

}
