/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.tests.process.OutputFiles;

import platform.tooling.support.ProcessStarters;
import platform.tooling.support.ThirdPartyJars;

class ModularCompilationTests {

	@Test
	void compileAllJUnitModules(@TempDir Path workspace, @FilePrefix("javac") OutputFiles javacOutputFiles) throws Exception {
		var lib = Files.createDirectories(workspace.resolve("lib"));
		ThirdPartyJars.copyAll(lib);

		var modules = List.of(
			// core
			"org.junit.platform.commons", "org.junit.platform.console", "org.junit.platform.engine",
			"org.junit.platform.launcher", "org.junit.platform.reporting",
			// suite
			"org.junit.platform.suite", "org.junit.platform.suite.api", "org.junit.platform.suite.engine",
			// jupiter
			"org.junit.jupiter", "org.junit.jupiter.api", "org.junit.jupiter.engine", "org.junit.jupiter.params");

		var result = ProcessStarters.javaCommand("javac") //
				.workingDir(workspace) //
				.addArguments("-d", workspace.resolve("classes").toAbsolutePath().toString()) //
				.addArguments("-Xlint:all", "-Werror")
				// external modules
				.addArguments("--module-path", lib.toAbsolutePath().toString())
				// source locations in module-specific form
				.addArguments("--module-source-path", moduleSourcePath("jupiter")) //
				.addArguments("--module-source-path", moduleSourcePath("jupiter-api")) //
				.addArguments("--module-source-path", moduleSourcePath("jupiter-engine")) //
				.addArguments("--module-source-path", moduleSourcePath("jupiter-migrationsupport")) //
				.addArguments("--module-source-path", moduleSourcePath("jupiter-params")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-commons")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-console")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-engine")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-launcher")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-reporting")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-suite")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-suite-api")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-suite-engine")) //
				.addArguments("--module-source-path", moduleSourcePath("platform-testkit")) //
				.addArguments("--module-source-path", moduleSourcePath("vintage-engine"))
				// un-shadow
				.addArguments("--add-modules", "info.picocli") //
				.addArguments("--add-reads", "org.junit.platform.console=info.picocli") //
				.addArguments("--add-modules", "org.opentest4j.reporting.events") //
				.addArguments("--add-reads", "org.junit.platform.reporting=org.opentest4j.reporting.events") //
				.addArguments("--add-modules", "de.siegmar.fastcsv") //
				.addArguments("--add-reads", "org.junit.jupiter.params=de.siegmar.fastcsv")
				// modules to compile
				.addArguments("--module", String.join(",", modules)) //
				.redirectOutput(javacOutputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
	}

	static String moduleSourcePath(String tag) {
		var name = "org.junit." + tag.replace('-', '.');
		var path = Path.of("../junit-%s/src/main/java".formatted(tag));
		var joiner = new StringJoiner(File.pathSeparator);
		joiner.add(path.toAbsolutePath().toString());
		var jte = Path.of("../junit-%s/build/generated/sources/jte/main".formatted(tag));
		if (Files.exists(jte))
			joiner.add(jte.toAbsolutePath().toString());
		return "%s=%s".formatted(name, joiner.toString());
	}
}
