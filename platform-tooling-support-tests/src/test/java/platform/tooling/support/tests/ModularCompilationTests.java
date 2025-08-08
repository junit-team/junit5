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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.spi.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModularCompilationTests {
	@Test
	void compileAllJUnitModules(@TempDir Path temp) throws Exception {
		var lib = Files.createDirectories(temp.resolve("lib"));
		//ThirdPartyJars.copyAll(lib);
		//ThirdPartyJars.copy(lib, "de.siegmar", "fastcsv"); // un-shadow
		//ThirdPartyJars.copy(lib, "info.picocli", "picocli"); // un-shadow
		downloadMissingExternalModules(lib);

		var modules = List.of(
			// core
			"org.junit.platform.commons", "org.junit.platform.console", "org.junit.platform.engine",
			"org.junit.platform.launcher", "org.junit.platform.reporting",
			// suite
			"org.junit.platform.suite", "org.junit.platform.suite.api", "org.junit.platform.suite.engine",
			// jupiter
			"org.junit.jupiter", "org.junit.jupiter.api", "org.junit.jupiter.engine", "org.junit.jupiter.params");
		Command.line("javac").add("-d", temp.resolve("classes")).add("-Xlint:all")
				// external modules
				.add("--module-path", lib)
				// source locations in module-specific form
				.add("--module-source-path", moduleSourcePath("jupiter")).add("--module-source-path",
					moduleSourcePath("jupiter-api")).add("--module-source-path",
						moduleSourcePath("jupiter-engine")).add("--module-source-path",
							moduleSourcePath("jupiter-migrationsupport")).add("--module-source-path",
								moduleSourcePath("jupiter-params")).add("--module-source-path",
									moduleSourcePath("platform-commons")).add("--module-source-path",
										moduleSourcePath("platform-console")).add("--module-source-path",
											moduleSourcePath("platform-engine")).add("--module-source-path",
												moduleSourcePath("platform-launcher")).add("--module-source-path",
													moduleSourcePath("platform-reporting")).add("--module-source-path",
														moduleSourcePath("platform-suite")).add("--module-source-path",
															moduleSourcePath("platform-suite-api")).add(
																"--module-source-path",
																moduleSourcePath("platform-suite-engine")).add(
																	"--module-source-path",
																	moduleSourcePath("platform-testkit")).add(
																		"--module-source-path",
																		moduleSourcePath("vintage-engine"))
				// un-shadow
				.add("--add-modules", "info.picocli").add("--add-reads", "org.junit.platform.console=info.picocli").add(
					"--add-modules", "de.siegmar.fastcsv").add("--add-reads",
						"org.junit.jupiter.params=de.siegmar.fastcsv")
				// modules to compile
				.add("--module", String.join(",", modules)).run();
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

	static void downloadMissingExternalModules(Path directory) throws Exception {
		var properties = new Properties();
		// Platform: commons + engine + launcher
		properties.setProperty("org.apiguardian.api",
			"https://repo1.maven.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar");
		properties.setProperty("org.jspecify",
			"https://repo1.maven.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar");
		properties.setProperty("org.opentest4j",
			"https://repo1.maven.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar");
		properties.setProperty("kotlin.stdlib",
			"https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.2.0/kotlin-stdlib-2.2.0.jar");
		properties.setProperty("kotlin.reflect",
			"https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-reflect/2.2.0/kotlin-reflect-2.2.0.jar");
		properties.setProperty("kotlinx.coroutines.core",
			"https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.10.2/kotlinx-coroutines-core-jvm-1.10.2.jar");
		// Platform: console + reporting
		properties.setProperty("org.opentest4j.reporting.tooling.spi",
			"https://repo1.maven.org/maven2/org/opentest4j/reporting/open-test-reporting-tooling-spi/0.2.4/open-test-reporting-tooling-spi-0.2.4.jar");
		properties.setProperty("info.picocli",
			"https://repo1.maven.org/maven2/info/picocli/picocli/4.7.7/picocli-4.7.7.jar");
		properties.setProperty("org.opentest4j.reporting.events",
			"https://repo1.maven.org/maven2/org/opentest4j/reporting/open-test-reporting-events/0.2.4/open-test-reporting-events-0.2.4.jar");
		properties.setProperty("org.opentest4j.reporting.schema",
			"https://repo1.maven.org/maven2/org/opentest4j/reporting/open-test-reporting-schema/0.2.4/open-test-reporting-schema-0.2.4.jar");
		// Jupiter
		properties.setProperty("de.siegmar.fastcsv",
			"https://repo1.maven.org/maven2/de/siegmar/fastcsv/4.0.0/fastcsv-4.0.0.jar");

		try (var http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
			for (var name : properties.stringPropertyNames()) {
				var target = directory.resolve(name + ".jar");
				if (Files.exists(target))
					continue;
				var request = HttpRequest.newBuilder(URI.create(properties.getProperty(name))).build();
				var response = http.send(request, HttpResponse.BodyHandlers.ofFile(target));
				System.out.println(response);
			}
		}
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
