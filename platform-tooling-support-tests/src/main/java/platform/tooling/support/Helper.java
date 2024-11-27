/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since 1.3
 */
public class Helper {

	public static final Duration TOOL_TIMEOUT = Duration.ofMinutes(3);

	private static final Path ROOT = Paths.get("..");
	private static final Path GRADLE_PROPERTIES = ROOT.resolve("gradle.properties");
	private static final Path SETTINGS_GRADLE = ROOT.resolve("settings.gradle.kts");

	private static final Properties gradleProperties = new Properties();

	static {
		try {
			gradleProperties.load(Files.newInputStream(GRADLE_PROPERTIES));
		}
		catch (Exception e) {
			throw new AssertionError("loading gradle.properties failed", e);
		}
	}

	public static String version(String module) {
		if (module.startsWith("junit-jupiter")) {
			return gradleProperties.getProperty("version");
		}
		if (module.startsWith("junit-platform")) {
			return gradleProperties.getProperty("platformVersion");
		}
		if (module.startsWith("junit-vintage")) {
			return gradleProperties.getProperty("vintageVersion");
		}
		throw new AssertionError("Unknown module: " + module);
	}

	static String groupId(String artifactId) {
		if (artifactId.startsWith("junit-jupiter")) {
			return "org.junit.jupiter";
		}
		if (artifactId.startsWith("junit-platform")) {
			return "org.junit.platform";
		}
		if (artifactId.startsWith("junit-vintage")) {
			return "org.junit.vintage";
		}
		return "org.junit";
	}

	public static String replaceVersionPlaceholders(String line) {
		line = line.replace("${jupiterVersion}", version("junit-jupiter"));
		line = line.replace("${vintageVersion}", version("junit-vintage"));
		line = line.replace("${platformVersion}", version("junit-platform"));
		return line;
	}

	public static List<String> loadModuleDirectoryNames() {
		var moduleLinePattern = Pattern.compile("include\\(\"(.+)\"\\)");
		try (var stream = Files.lines(SETTINGS_GRADLE) //
				.map(moduleLinePattern::matcher) //
				.filter(Matcher::matches) //
				.map(matcher -> matcher.group(1)) //
				.filter(name -> name.startsWith("junit-")) //
				.filter(name -> !name.equals("junit-bom")) //
				.filter(name -> !name.equals("junit-platform-console-standalone"))) {
			return stream.collect(Collectors.toList());
		}
		catch (Exception e) {
			throw new AssertionError("loading module directory names failed: " + SETTINGS_GRADLE);
		}
	}

	static JarFile createJarFile(String module) {
		var path = MavenRepo.jar(module);
		try {
			return new JarFile(path.toFile());
		}
		catch (IOException e) {
			throw new UncheckedIOException("Creating JarFile for '" + path + "' failed.", e);
		}
	}

	public static List<JarFile> loadJarFiles() {
		return loadModuleDirectoryNames().stream().map(Helper::createJarFile).collect(Collectors.toList());
	}

	public static Optional<Path> getJavaHome(String version) {
		// First, try various system sources...
		var sources = Stream.of( //
			System.getProperty("java.home." + version), //
			System.getProperty("java." + version), //
			System.getProperty("jdk.home." + version), //
			System.getProperty("jdk." + version), //
			System.getenv("JAVA_HOME_" + version), //
			System.getenv("JAVA_" + version), //
			System.getenv("JDK" + version) //
		);
		return sources.filter(Objects::nonNull).findFirst().map(Path::of);
	}

	/** Load, here copy, modular jar files to the given target directory. */
	public static void loadAllJUnitModules(Path target) throws Exception {
		for (var module : loadModuleDirectoryNames()) {
			var jar = MavenRepo.jar(module);
			Files.copy(jar, target.resolve(jar.getFileName()));
		}
	}

	/** Walk directory tree structure. */
	public static List<String> treeWalk(Path root) {
		var lines = new ArrayList<String>();
		treeWalk(root, lines::add);
		return lines;
	}

	/** Walk directory tree structure. */
	public static void treeWalk(Path root, Consumer<String> out) {
		try (var stream = Files.walk(root)) {
			stream.map(root::relativize) //
					.map(path -> path.toString().replace('\\', '/')) //
					.sorted().filter(Predicate.not(String::isEmpty)) //
					.forEach(out);
		}
		catch (Exception e) {
			throw new Error("Walking tree failed: " + root, e);
		}
	}
}
