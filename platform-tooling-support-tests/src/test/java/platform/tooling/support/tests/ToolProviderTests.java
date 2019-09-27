/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import platform.tooling.support.Helper;

/**
 * @since 1.6
 */
class ToolProviderTests {

	private static void assertJUnitPrintsHelpMessage(ToolProvider junit) {
		var out = new StringWriter();
		var err = new StringWriter();
		var code = junit.run(new PrintWriter(out), new PrintWriter(err), "--help");
		assertAll(() -> assertLinesMatch(List.of( //
			">> USAGE >>", //
			"Launches the JUnit Platform from the console.", //
			">> OPTIONS >>"), //
			out.toString().lines().collect(Collectors.toList())), //
			() -> assertEquals("", err.toString()), //
			() -> assertEquals(0, code, "Expected exit of 0, but got: " + code) //
		);
	}

	@Test
	void findAndRunJUnitOnTheModulePath(@TempDir Path temp) throws Exception {
		// Prepare the following modules:
		//
		// org.apiguardian.api
		// org.junit.platform.commons
		// org.junit.platform.console
		// org.junit.platform.engine
		// org.junit.platform.launcher
		// org.junit.platform.reporting
		// org.junit.platform.runner
		// org.junit.platform.suite.api
		// org.junit.platform.testkit
		// org.opentest4j
		var lib = Files.createDirectories(temp.resolve("lib"));
		for (var module : Helper.loadModuleDirectoryNames()) {
			if (module.startsWith("junit-platform")) {
				var jar = Helper.createJarPath(module);
				Files.copy(jar, lib.resolve(jar.getFileName()));
			}
		}
		Helper.load(lib, "org.apiguardian", "apiguardian-api", Helper.version("apiGuardian", "1.1.0"));
		Helper.load(lib, "org.opentest4j", "opentest4j", Helper.version("ota4j", "1.2.0"));

		try {
			checkModulePath(lib);
		}
		finally {
			// Work around https://bugs.openjdk.java.net/browse/JDK-8224794
			if (OS.WINDOWS.isCurrentOs()) {
				System.gc();
				Thread.sleep(1234);
			}
		}
	}

	private void checkModulePath(Path lib) {
		var finder = ModuleFinder.of(lib);
		var modules = finder.findAll().stream() //
				.map(ModuleReference::descriptor) //
				.map(ModuleDescriptor::toNameAndVersion) //
				.sorted() //
				.collect(Collectors.toList());
		// modules.forEach(System.out::println);

		var bootLayer = ModuleLayer.boot();
		var configuration = bootLayer.configuration().resolveAndBind(finder, ModuleFinder.of(), Set.of());
		var layer = bootLayer.defineModulesWithOneLoader(configuration, ClassLoader.getPlatformClassLoader());

		var sl = ServiceLoader.load(layer, ToolProvider.class);
		var junit = StreamSupport.stream(sl.spliterator(), false).filter(p -> p.name().equals("junit")).findFirst();

		assertTrue(junit.isPresent(), "Tool 'junit' not found in modules: " + modules);
		assertJUnitPrintsHelpMessage(junit.get());
	}

}
