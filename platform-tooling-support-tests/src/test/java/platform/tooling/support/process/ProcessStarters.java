/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.process;

import java.nio.file.Path;
import java.util.Optional;

import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;

public class ProcessStarters {

	public static ProcessStarter java() {
		return javaCommand(currentJdkHome(), "java");
	}

	private static Path currentJdkHome() {
		var executable = ProcessHandle.current().info().command().map(Path::of).orElseThrow();
		// path element count is 3 or higher: "<JAVA_HOME>/bin/java[.exe]"
		return executable.getParent().getParent().toAbsolutePath();
	}

	public static ProcessStarter java(Path javaHome) {
		return javaCommand(javaHome, "java");
	}

	public static ProcessStarter javaCommand(String commandName) {
		return javaCommand(currentJdkHome(), commandName);
	}

	public static ProcessStarter javaCommand(Path javaHome, String commandName) {
		return new ProcessStarter() //
				.executable(javaHome.resolve("bin").resolve(commandName)) //
				.putEnvironment("JAVA_HOME", javaHome);
	}

	public static ProcessStarter gradlew() {
		var starter = new ProcessStarter() //
				.executable(Path.of("..").resolve("gradlew")) //
				.putEnvironment("JAVA_HOME", getGradleJavaHome().orElseThrow(TestAbortedException::new));
		return withCommonEnvironmentVariables(starter);
	}

	private static ProcessStarter withCommonEnvironmentVariables(ProcessStarter starter) {
		starter.putEnvironment("JUNIT_JUPITER_VERSION", Helper.version("junit-jupiter"));
		starter.putEnvironment("JUNIT_VINTAGE_VERSION", Helper.version("junit-vintage"));
		starter.putEnvironment("JUNIT_PLATFORM_VERSION", Helper.version("junit-platform"));
		return starter;
	}

	public static Optional<Path> getGradleJavaHome() {
		return Helper.getJavaHome(System.getProperty("gradle.java.version"));
	}
}
