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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.spi.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import platform.tooling.support.ThirdPartyJars;

class ModularCompilationTests {

	@Test
	void compileAllJUnitModules(@TempDir Path temp) throws Exception {
		var lib = Files.createDirectories(temp.resolve("lib"));
		ThirdPartyJars.copyAll(lib);

		var modules = List.of(
			// core
			"org.junit.platform.commons", "org.junit.platform.console", "org.junit.platform.engine",
			"org.junit.platform.launcher", "org.junit.platform.reporting",
			// suite
			"org.junit.platform.suite", "org.junit.platform.suite.api", "org.junit.platform.suite.engine",
			// jupiter
			"org.junit.jupiter", "org.junit.jupiter.api", "org.junit.jupiter.engine", "org.junit.jupiter.params");
		Command.line("javac") //
				.add("-d", temp.resolve("classes")) //
				.add("-Xlint:all")
				// external modules
				.add("--module-path", lib)
				// source locations in module-specific form
				.add("--module-source-path", moduleSourcePath("jupiter")) //
				.add("--module-source-path", moduleSourcePath("jupiter-api")) //
				.add("--module-source-path", moduleSourcePath("jupiter-engine")) //
				.add("--module-source-path", moduleSourcePath("jupiter-migrationsupport")) //
				.add("--module-source-path", moduleSourcePath("jupiter-params")) //
				.add("--module-source-path", moduleSourcePath("platform-commons")) //
				.add("--module-source-path", moduleSourcePath("platform-console")) //
				.add("--module-source-path", moduleSourcePath("platform-engine")) //
				.add("--module-source-path", moduleSourcePath("platform-launcher")) //
				.add("--module-source-path", moduleSourcePath("platform-reporting")) //
				.add("--module-source-path", moduleSourcePath("platform-suite")) //
				.add("--module-source-path", moduleSourcePath("platform-suite-api")) //
				.add("--module-source-path", moduleSourcePath("platform-suite-engine")) //
				.add("--module-source-path", moduleSourcePath("platform-testkit")) //
				.add("--module-source-path", moduleSourcePath("vintage-engine"))
				// un-shadow
				.add("--add-modules", "info.picocli") //
				.add("--add-reads", "org.junit.platform.console=info.picocli") //
				.add("--add-modules", "org.opentest4j.reporting.events") //
				.add("--add-reads", "org.junit.platform.reporting=org.opentest4j.reporting.events") //
				.add("--add-modules", "de.siegmar.fastcsv") //
				.add("--add-reads", "org.junit.jupiter.params=de.siegmar.fastcsv")
				// modules to compile
				.add("--module", String.join(",", modules)) //
				.run();
	}

	static String moduleSourcePath(String tag) {
		var name = "org.junit." + tag.replace('-', '.');
		var path = Path.of("../junit-%s/src/main/java".formatted(tag));
		var joiner = new StringJoiner(File.pathSeparator);
		joiner.add(path.toString());
		var jte = Path.of("../junit-%s/build/generated/sources/jte/main".formatted(tag));
		if (Files.exists(jte))
			joiner.add(jte.toString());
		return "%s=%s".formatted(name, joiner.toString());
	}

	record Command(String name, List<String> arguments) implements Runnable {
		public static Command line(String name) {
			return new Command(name, new ArrayList<>());
		}

		Command add(Object argument) {
			arguments.add(argument.toString());
			return this;
		}

		Command add(String key, Object value, Object... more) {
			add(key).add(value);
			if (more.length == 0)
				return this;
			if (more.length == 1)
				return add(more[0]);
			if (more.length == 2)
				return add(more[0]).add(more[1]);
			for (var next : more)
				add(next);
			return this;
		}

		@Override
		public void run() {
			System.out.println("| " + name + " " + String.join(" ", arguments));
			var tool = ToolProvider.findFirst(name);
			if (tool.isPresent()) {
				var args = arguments.toArray(String[]::new);
				var code = tool.get().run(System.out, System.err, args);
				if (code == 0)
					return;
				throw new RuntimeException(name + " returned non-zero exit code: " + code);
			}
			var program = Path.of(System.getProperty("java.home"), "bin", name);
			var builder = new ProcessBuilder(program.toString());
			try {
				builder.command().addAll(arguments);
				var process = builder.inheritIO().start();
				var code = process.waitFor();
				if (code == 0)
					return;
				throw new RuntimeException(name + " returned non-zero exit code: " + code);
			}
			catch (Exception exception) {
				throw new RuntimeException(name + " failed.", exception);
			}
		}
	}
}
