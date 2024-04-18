/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.time.Duration;

import de.sormuras.bartholdy.tool.GradleWrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.9.1
 */
@EnabledIfEnvironmentVariable(named = "GRAALVM_HOME", matches = ".+")
class GraalVmStarterTests {

	@Test
	void runsTestsInNativeImage() {
		var request = Request.builder() //
				.setTool(new GradleWrapper(Paths.get(".."))) //
				.setProject("graalvm-starter") //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("javaToolchains", "nativeTest", "--no-daemon", "--stacktrace") //
				.setTimeout(Duration.ofMinutes(10)) //
				.build();

		var result = request.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode());
		assertThat(result.getOutputLines("out")) //
				.anyMatch(line -> line.contains("CalculatorTests > 1 + 1 = 2 SUCCESSFUL")) //
				.anyMatch(line -> line.contains("CalculatorTests > 1 + 100 = 101 SUCCESSFUL")) //
				.anyMatch(line -> line.contains("ClassLevelAnnotationTests$Inner > test() SUCCESSFUL")) //
				.anyMatch(line -> line.contains("BUILD SUCCESSFUL"));
	}
}
