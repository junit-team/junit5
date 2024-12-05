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

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.condition.OS;
import org.junit.platform.tests.process.ProcessStarter;
import org.opentest4j.TestAbortedException;

public class ProcessStarters {

	public static ProcessStarter java() {
		return javaCommand(currentJdkHome(), "java");
	}

	public static Path currentJdkHome() {
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
		return new ProcessStarter() //
				.executable(Path.of("..").resolve(windowsOrOtherExecutable("gradlew.bat", "gradlew")).toAbsolutePath()) //
				.putEnvironment("JAVA_HOME", getGradleJavaHome().orElseThrow(TestAbortedException::new)) //
				.addArguments("-PjupiterVersion=" + Helper.version("junit-jupiter")) //
				.addArguments("-PvintageVersion=" + Helper.version("junit-vintage")) //
				.addArguments("-PplatformVersion=" + Helper.version("junit-platform"));
	}

	public static ProcessStarter maven() {
		return maven(currentJdkHome());
	}

	public static ProcessStarter maven(Path javaHome) {
		return new ProcessStarter() //
				.executable(Path.of(System.getProperty("mavenDistribution")).resolve("bin").resolve(
					windowsOrOtherExecutable("mvn.cmd", "mvn")).toAbsolutePath()) //
				.putEnvironment("JAVA_HOME", javaHome) //
				.addArguments("-Djunit.jupiter.version=" + Helper.version("junit-jupiter")) //
				.addArguments("-Djunit.bom.version=" + Helper.version("junit-jupiter")) //
				.addArguments("-Djunit.vintage.version=" + Helper.version("junit-vintage")) //
				.addArguments("-Djunit.platform.version=" + Helper.version("junit-platform"));
	}

	private static String windowsOrOtherExecutable(String cmdOrExe, String other) {
		return OS.current() == OS.WINDOWS ? cmdOrExe : other;
	}

	public static Optional<Path> getGradleJavaHome() {
		return Helper.getJavaHome(System.getProperty("gradle.java.version"));
	}
}
