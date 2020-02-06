/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import platform.tooling.support.Helper;

/**
 * @since 1.5
 */
class JavacModulesTests {

	private static final Pattern MODULE_NAME_PATTERN = Pattern.compile("(module)\\s+(.+)\\s*\\{.*");

	private static class Project {
		final Path moduleInfo;
		final Path moduleSource;
		final String moduleName;
		final Path javaClassesDir;

		Project(Path moduleInfo) {
			this.moduleInfo = moduleInfo;
			this.moduleSource = moduleInfo.getParent().getParent();
			this.javaClassesDir = moduleSource.getParent().getParent().resolve("build/classes/java");
			try {
				var nameMatcher = MODULE_NAME_PATTERN.matcher(Files.readString(moduleInfo));
				if (!nameMatcher.find()) {
					throw new Error("Expected java module descriptor unit, but got: " + moduleInfo);
				}
				this.moduleName = nameMatcher.group(2).trim();
			}
			catch (Exception e) {
				throw new Error("Extracting module name failed", e);
			}
		}

		private Stream<Path> getMainOutputDirs() {
			try {
				return Files.list(javaClassesDir) //
						.filter(file -> Files.isDirectory(file)) //
						.filter(dir -> dir.getFileName().toString().startsWith("main"));
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private static List<String> compileModules(Path temp, Writer out, Writer err, Function<Project, Stream<Path>> patch)
			throws Exception {
		var args = new ArrayList<String>();
		args.add("-Xlint:all,-requires-automatic,-requires-transitive-automatic");
		args.add("-proc:none"); // disable annotation processing
		args.add("-cp");
		args.add(""); // set empty class path, otherwise system property "java.class.path" is read

		args.add("-d");
		args.add(temp.resolve("destination").toString());

		var lib = temp.resolve("lib");
		Files.createDirectories(lib);
		Helper.load(lib, "junit", "junit", "4.12");
		Helper.load(lib, "org.assertj", "assertj-core", "3.13.2");
		Helper.load(lib, "org.apiguardian", "apiguardian-api", "1.1.0");
		Helper.load(lib, "org.opentest4j", "opentest4j", "1.2.0");
		args.add("--module-path");
		args.add(lib.toString());

		var base = Path.of("..").toAbsolutePath().normalize();
		try (var walk = Files.walk(base)) {
			var projects = walk.filter(path -> path.endsWith("module-info.java")) //
					.map(base::relativize) //
					.filter(path -> !path.startsWith("platform-tooling-support-tests")) //
					.map(base::resolve) //
					.map(Project::new) //
					.collect(Collectors.toList());
			assertEquals(14, projects.size());

			args.add("--module-source-path");
			args.add(projects.stream() //
					.map(project -> project.moduleSource) //
					.map(Object::toString) //
					.collect(joining(File.pathSeparator)));

			for (var project : projects) {
				var path = patch.apply(project).map(Path::toString).collect(joining(File.pathSeparator));
				if (path.isEmpty()) {
					continue;
				}
				args.add("--patch-module");
				args.add(project.moduleName + "=" + path);
			}

			projects.forEach(project -> args.add(project.moduleInfo.toString()));

			var javac = ToolProvider.findFirst("javac").orElseThrow();
			javac.run(new PrintWriter(out), new PrintWriter(err), args.toArray(String[]::new));

			assertTrue(out.toString().isBlank(), out.toString());
		}
		return args;
	}

	@Test
	void patchMainClasses(@TempDir Path temp) throws Exception {
		var out = new StringWriter();
		var err = new StringWriter();

		var args = compileModules(temp, out, err, Project::getMainOutputDirs);

		assertTrue(err.toString().isBlank(), () -> err.toString() + "\n\n" + String.join("\n", args));

		var listing = Helper.treeWalk(temp);
		assertLinesMatch(List.of( //
			"destination", //
			">> CLASSES >>", //
			"lib", //
			"lib/apiguardian-api-1.1.0.jar", //
			"lib/assertj-core-3.13.2.jar", //
			"lib/junit-4.12.jar", //
			"lib/opentest4j-1.2.0.jar"), listing);
	}

}
