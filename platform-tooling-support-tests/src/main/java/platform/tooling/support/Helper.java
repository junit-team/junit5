/*
 * Copyright 2015-2020 the original author or authors.
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

/**
 * @since 1.3
 */
public class Helper {

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

	public static String version(String moduleOrSystemProperty) {
		return version(moduleOrSystemProperty, "<no default version specified>");
	}

	public static String version(String moduleOrSystemProperty, String defaultVersion) {
		if (moduleOrSystemProperty.startsWith("junit-jupiter")) {
			return gradleProperties.getProperty("version");
		}
		if (moduleOrSystemProperty.startsWith("junit-platform")) {
			return gradleProperties.getProperty("platformVersion");
		}
		if (moduleOrSystemProperty.startsWith("junit-vintage")) {
			return gradleProperties.getProperty("vintageVersion");
		}
		return System.getProperty("Versions." + moduleOrSystemProperty, defaultVersion);
	}

	public static String replaceVersionPlaceholders(String line) {
		line = line.replace("${jupiterVersion}", version("junit-jupiter"));
		line = line.replace("${vintageVersion}", version("junit-vintage"));
		line = line.replace("${platformVersion}", version("junit-platform"));
		return line;
	}

	public static List<String> loadModuleDirectoryNames() {
		Pattern moduleLinePattern = Pattern.compile("include\\(\"(.+)\"\\)");
		try (Stream<String> stream = Files.lines(SETTINGS_GRADLE) //
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

	public static Path createJarPath(String module) {
		var parent = Paths.get("..", module, "build", "libs");
		Path jar = parent.resolve(module + '-' + version(module) + ".jar");
		Path shadowJar = parent.resolve(module + '-' + version(module) + "-all.jar");
		return Files.exists(jar) ? jar : shadowJar;
	}

	static JarFile createJarFile(String module) {
		var path = createJarPath(module);
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
		List<Supplier<String>> sources = List.of( //
			() -> System.getProperty("java.home." + version), //
			() -> System.getProperty("java." + version), //
			() -> System.getProperty("jdk.home." + version), //
			() -> System.getProperty("jdk." + version), //
			() -> System.getenv("JAVA_HOME_" + version), //
			() -> System.getenv("JAVA_" + version) //
		);
		var home = sources.stream().map(Supplier::get).filter(Objects::nonNull).findFirst();
		// If no java home set then inspect Maven Toolchains configuration file...
		return home.map(h -> Path.of(h)).or(() -> getJdkHomeFromMavenToolchains(version));
	}

	// https://maven.apache.org/guides/mini/guide-using-toolchains.html
	static Optional<Path> getJdkHomeFromMavenToolchains(String version) {
		var mavenToolChains = Path.of(System.getProperty("user.home"), ".m2", "toolchains.xml");
		if (!Files.isRegularFile(mavenToolChains)) {
			return Optional.empty();
		}
		try {
			var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			var document = builder.parse(mavenToolChains.toFile());
			var xpath = XPathFactory.newInstance().newXPath();
			var jdkHome = xpath.evaluate("//toolchains" //
					+ "/toolchain[descendant::type[text()='jdk']]" //
					+ "/provides[descendant::version[text()='" + version + "']]" //
					+ "/.." //
					+ "/configuration/jdkHome",
				document);
			if (!jdkHome.isBlank()) {
				return Optional.of(Path.of(jdkHome));
			}
		}
		catch (Exception e) {
			// ignore
		}
		return Optional.empty();
	}

	/** Load, here copy, modular jar files to the given target directory. */
	public static void loadAllJUnitModules(Path target) throws Exception {
		for (var module : loadModuleDirectoryNames()) {
			var jar = createJarPath(module);
			Files.copy(jar, target.resolve(jar.getFileName()));
		}
	}

	/** Load single JAR from Maven Central. */
	public static void load(Path target, String group, String artifact, String version) throws Exception {
		var jar = String.format("%s-%s.jar", artifact, version);
		var mvn = "https://repo1.maven.org/maven2/";
		var grp = group.replace('.', '/');
		var url = new URL(mvn + String.join("/", grp, artifact, version, jar));
		try (var stream = url.openStream()) {
			Files.copy(stream, target.resolve(jar), StandardCopyOption.REPLACE_EXISTING);
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
