/*
 * Copyright 2015-2024 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.jdk.Jar;
import de.sormuras.bartholdy.jdk.Javac;
import de.sormuras.bartholdy.tool.Java;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.4
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StandaloneTests {

	@Test
	void jarFileWithoutCompiledModuleDescriptorClass() throws Exception {
		var jar = MavenRepo.jar("junit-platform-console-standalone");
		var name = "module-info.class";
		var found = new ArrayList<Path>();
		try (var fileSystem = FileSystems.newFileSystem(jar)) {
			for (var rootDirectory : fileSystem.getRootDirectories()) {
				try (var stream = Files.walk(rootDirectory)) {
					stream.filter(path -> path.getNameCount() > 0) // skip root entry
							.filter(path -> path.getFileName().toString().equals(name)).forEach(found::add);
				}
			}
		}
		assertTrue(found.isEmpty(), jar + " must not contain any " + name + " files: " + found);
	}

	@Test
	void listAllObservableEngines() {
		var result = Request.builder() //
				.setTool(new Java()) //
				.setProject("standalone") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone")) //
				.addArguments("engines", "--disable-ansi-colors", "--disable-banner").build() //
				.run(false);

		assertEquals(0, result.getExitCode(), () -> getExitCodeMessage(result));

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var suiteVersion = Helper.version("junit-platform-suite-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertLinesMatch("""
				junit-jupiter (org.junit.jupiter:junit-jupiter-engine:%s)
				junit-platform-suite (org.junit.platform:junit-platform-suite-engine:%s)
				junit-vintage (org.junit.vintage:junit-vintage-engine:%s)
				""".formatted(jupiterVersion, suiteVersion, vintageVersion).lines(), //
			result.getOutput("out").lines());
	}

	@Test
	@Order(1)
	void compile() throws Exception {
		var workspace = Request.WORKSPACE.resolve("standalone");
		var result = Request.builder() //
				.setTool(new Javac()) //
				.setProject("standalone") //
				.addArguments("-Xlint:-options") //
				.addArguments("--release", "8") //
				.addArguments("-proc:none") //
				.addArguments("-d", workspace.resolve("bin")) //
				.addArguments("--class-path", MavenRepo.jar("junit-platform-console-standalone")) //
				.addArguments(workspace.resolve("src/standalone/JupiterIntegration.java")) //
				.addArguments(workspace.resolve("src/standalone/JupiterParamsIntegration.java")) //
				.addArguments(workspace.resolve("src/standalone/SuiteIntegration.java")) //
				.addArguments(workspace.resolve("src/standalone/VintageIntegration.java")).build() //
				.run();

		assertEquals(0, result.getExitCode(), () -> getExitCodeMessage(result));
		assertTrue(result.getOutput("out").isEmpty());
		assertTrue(result.getOutput("err").isEmpty());

		// create "tests.jar" that'll be picked-up by "testWithJarredTestClasses()" later
		var jarFolder = Files.createDirectories(workspace.resolve("jar"));
		var jarResult = Request.builder() //
				.setTool(new Jar()) //
				.setProject("standalone") //
				.addArguments("--create") //
				.addArguments("--file", jarFolder.resolve("tests.jar")) //
				.addArguments("-C", workspace.resolve("bin"), ".") //
				.build().run(false);
		assertEquals(0, jarResult.getExitCode(), String.join("\n", jarResult.getOutputLines("out")));
	}

	@Test
	@Order(2)
	void discoverTree() {
		Result result = discover("--details-theme=ascii");

		var expected = """
				.
				+-- JUnit Platform Suite
				| '-- SuiteIntegration
				|   '-- JUnit Jupiter
				|     '-- SuiteIntegration$SingleTestContainer
				|       '-- successful()
				+-- JUnit Jupiter
				| +-- JupiterIntegration
				| | +-- successful()
				| | +-- fail()
				| | +-- abort()
				| | '-- disabled()
				| +-- SuiteIntegration$SingleTestContainer
				| | '-- successful()
				| '-- JupiterParamsIntegration
				|   '-- parameterizedTest(String)
				'-- JUnit Vintage
				  '-- VintageIntegration
				    +-- f4il
				    +-- ignored
				    '-- succ3ssful

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.getOutputLines("out").stream());
	}

	@Test
	@Order(2)
	void discoverFlat() {
		Result result = discover("--details=flat");

		var expected = """
				JUnit Platform Suite ([engine:junit-platform-suite])
				SuiteIntegration ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration])
				JUnit Jupiter ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter])
				SuiteIntegration$SingleTestContainer ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer])
				successful() ([engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()])
				JUnit Jupiter ([engine:junit-jupiter])
				JupiterIntegration ([engine:junit-jupiter]/[class:standalone.JupiterIntegration])
				successful() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:successful()])
				fail() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:fail()])
				abort() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:abort()])
				disabled() ([engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:disabled()])
				SuiteIntegration$SingleTestContainer ([engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer])
				successful() ([engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()])
				JupiterParamsIntegration ([engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration])
				parameterizedTest(String) ([engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration]/[test-template:parameterizedTest(java.lang.String)])
				JUnit Vintage ([engine:junit-vintage])
				VintageIntegration ([engine:junit-vintage]/[runner:standalone.VintageIntegration])
				f4il ([engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:f4il(standalone.VintageIntegration)])
				ignored ([engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:ignored(standalone.VintageIntegration)])
				succ3ssful ([engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:succ3ssful(standalone.VintageIntegration)])

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.getOutputLines("out").stream());
	}

	@Test
	@Order(2)
	void discoverVerbose() {
		Result result = discover("--details=verbose", "--details-theme=ascii");

		var expected = """
				+-- JUnit Platform Suite
				| +-- SuiteIntegration
				| | +-- JUnit Jupiter
				| | | +-- SuiteIntegration$SingleTestContainer
				| | | | +-- successful()
				| | | | |      tags: []
				| | | | |  uniqueId: [engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()]
				| | | | |    parent: [engine:junit-platform-suite]/[suite:standalone.SuiteIntegration]/[engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]
				| | | | |    source: MethodSource [className = 'standalone.SuiteIntegration$SingleTestContainer', methodName = 'successful', methodParameterTypes = '']
				| | | '-- SuiteIntegration$SingleTestContainer
				| | '-- JUnit Jupiter
				| '-- SuiteIntegration
				'-- JUnit Platform Suite
				+-- JUnit Jupiter
				| +-- JupiterIntegration
				| | +-- successful()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:successful()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'successful', methodParameterTypes = '']
				| | +-- fail()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:fail()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'fail', methodParameterTypes = '']
				| | +-- abort()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:abort()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'abort', methodParameterTypes = '']
				| | +-- disabled()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]/[method:disabled()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterIntegration', methodName = 'disabled', methodParameterTypes = '']
				| '-- JupiterIntegration
				| +-- SuiteIntegration$SingleTestContainer
				| | +-- successful()
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]/[method:successful()]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.SuiteIntegration$SingleTestContainer]
				| | |    source: MethodSource [className = 'standalone.SuiteIntegration$SingleTestContainer', methodName = 'successful', methodParameterTypes = '']
				| '-- SuiteIntegration$SingleTestContainer
				| +-- JupiterParamsIntegration
				| | +-- parameterizedTest(String)
				| | |      tags: []
				| | |  uniqueId: [engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration]/[test-template:parameterizedTest(java.lang.String)]
				| | |    parent: [engine:junit-jupiter]/[class:standalone.JupiterParamsIntegration]
				| | |    source: MethodSource [className = 'standalone.JupiterParamsIntegration', methodName = 'parameterizedTest', methodParameterTypes = 'java.lang.String']
				| '-- JupiterParamsIntegration
				'-- JUnit Jupiter
				+-- JUnit Vintage
				| +-- VintageIntegration
				| | +-- f4il
				| | |      tags: []
				| | |  uniqueId: [engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:f4il(standalone.VintageIntegration)]
				| | |    parent: [engine:junit-vintage]/[runner:standalone.VintageIntegration]
				| | |    source: MethodSource [className = 'standalone.VintageIntegration', methodName = 'f4il', methodParameterTypes = '']
				| | +-- ignored
				| | |      tags: []
				| | |  uniqueId: [engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:ignored(standalone.VintageIntegration)]
				| | |    parent: [engine:junit-vintage]/[runner:standalone.VintageIntegration]
				| | |    source: MethodSource [className = 'standalone.VintageIntegration', methodName = 'ignored', methodParameterTypes = '']
				| | +-- succ3ssful
				| | |      tags: []
				| | |  uniqueId: [engine:junit-vintage]/[runner:standalone.VintageIntegration]/[test:succ3ssful(standalone.VintageIntegration)]
				| | |    parent: [engine:junit-vintage]/[runner:standalone.VintageIntegration]
				| | |    source: MethodSource [className = 'standalone.VintageIntegration', methodName = 'succ3ssful', methodParameterTypes = '']
				| '-- VintageIntegration
				'-- JUnit Vintage

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.getOutputLines("out").stream());
	}

	@Test
	@Order(2)
	void discoverNone() {
		Result result = discover("--details=none");

		assertThat(result.getOutputLines("out")).isEmpty();
	}

	@Test
	@Order(2)
	void discoverSummary() {
		Result result = discover("--details=summary");

		var expected = """

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();
		assertLinesMatch(expected.lines(), result.getOutputLines("out").stream());
	}

	@Test
	@Order(2)
	void discoverTestFeed() {
		Result result = discover("--details=testfeed");
		var expected = """
				JUnit Platform Suite > SuiteIntegration > JUnit Jupiter > SuiteIntegration$SingleTestContainer > successful()
				JUnit Jupiter > JupiterIntegration > successful()
				JUnit Jupiter > JupiterIntegration > fail()
				JUnit Jupiter > JupiterIntegration > abort()
				JUnit Jupiter > JupiterIntegration > disabled()
				JUnit Jupiter > SuiteIntegration$SingleTestContainer > successful()
				JUnit Vintage > VintageIntegration > f4il
				JUnit Vintage > VintageIntegration > ignored
				JUnit Vintage > VintageIntegration > succ3ssful

				[        11 containers found ]
				[         9 tests found      ]

				""".stripIndent();

		assertLinesMatch(expected.lines(), result.getOutputLines("out").stream());
	}

	private static Result discover(String... args) {
		var result = Request.builder() //
				.setTool(new Java()) //
				.setProject("standalone") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone")) //
				.addArguments("discover") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--disable-ansi-colors") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin") //
				.addArguments((Object[]) args) //
				.build() //
				.run(false);

		assertEquals(0, result.getExitCode(), () -> getExitCodeMessage(result));
		return result;
	}

	@Test
	@Order(3)
	void execute() throws IOException {
		var result = Request.builder() //
				.setTool(new Java()) //
				.setProject("standalone") //
				.addArguments("--show-version") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("-Djunit.platform.launcher.interceptors.enabled=true") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone")) //
				.addArguments("execute") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin").build() //
				.run(false);

		assertEquals(1, result.getExitCode(), () -> getExitCodeMessage(result));

		var workspace = Request.WORKSPACE.resolve("standalone");
		var expectedOutLines = Files.readAllLines(workspace.resolve("expected-out.txt"));
		var expectedErrLines = Files.readAllLines(workspace.resolve("expected-err.txt"));
		assertLinesMatch(expectedOutLines, result.getOutputLines("out"));
		assertLinesMatch(expectedErrLines, result.getOutputLines("err"));

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertTrue(result.getOutput("err").contains("junit-jupiter"
				+ " (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: " + jupiterVersion));
		assertTrue(result.getOutput("err").contains("junit-vintage"
				+ " (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: " + vintageVersion));
	}

	@Test
	@Order(4)
	void executeOnJava8() throws IOException {
		Java java8 = getJava8();
		var result = Request.builder() //
				.setTool(java8) //
				.setJavaHome(java8.getHome()) //
				.setProject("standalone") //
				.addArguments("-showversion") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("-Djunit.platform.launcher.interceptors.enabled=true") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone")) //
				.addArguments("execute") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin").build() //
				.run(false);

		assertEquals(1, result.getExitCode(), () -> getExitCodeMessage(result));

		var workspace = Request.WORKSPACE.resolve("standalone");
		var expectedOutLines = Files.readAllLines(workspace.resolve("expected-out.txt"));
		var expectedErrLines = getExpectedErrLinesOnJava8(workspace);
		assertLinesMatch(expectedOutLines, result.getOutputLines("out"));
		assertLinesMatch(expectedErrLines, result.getOutputLines("err"));

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertTrue(result.getOutput("err").contains("junit-jupiter"
				+ " (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: " + jupiterVersion));
		assertTrue(result.getOutput("err").contains("junit-vintage"
				+ " (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: " + vintageVersion));
	}

	@Test
	@Order(5)
	// https://github.com/junit-team/junit5/issues/2600
	void executeOnJava8SelectPackage() throws IOException {
		Java java8 = getJava8();
		var result = Request.builder() //
				.setTool(java8) //
				.setJavaHome(java8.getHome()) //
				.setProject("standalone") //
				.addArguments("-showversion") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("-Djunit.platform.launcher.interceptors.enabled=true") //
				.addArguments("-jar", MavenRepo.jar("junit-platform-console-standalone")) //
				.addArguments("execute") //
				.addArguments("--select-package", "standalone") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin").build() //
				.run(false);

		assertEquals(1, result.getExitCode(), () -> getExitCodeMessage(result));

		var workspace = Request.WORKSPACE.resolve("standalone");
		var expectedOutLines = Files.readAllLines(workspace.resolve("expected-out.txt"));
		var expectedErrLines = getExpectedErrLinesOnJava8(workspace);
		assertLinesMatch(expectedOutLines, result.getOutputLines("out"));
		assertLinesMatch(expectedErrLines, result.getOutputLines("err"));

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertTrue(result.getOutput("err").contains("junit-jupiter"
				+ " (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: " + jupiterVersion));
		assertTrue(result.getOutput("err").contains("junit-vintage"
				+ " (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: " + vintageVersion));
	}

	private static List<String> getExpectedErrLinesOnJava8(Path workspace) throws IOException {
		var expectedErrLines = new ArrayList<String>();
		expectedErrLines.add(">> JAVA VERSION >>");
		expectedErrLines.addAll(Files.readAllLines(workspace.resolve("expected-err.txt")));
		return expectedErrLines;
	}

	@Test
	@Order(6)
	@Disabled("https://github.com/junit-team/junit5/issues/1724")
	void executeWithJarredTestClasses() {
		var jar = MavenRepo.jar("junit-platform-console-standalone");
		var path = new ArrayList<String>();
		// path.add("bin"); // "exploded" test classes are found, see also test() above
		path.add(Request.WORKSPACE.resolve("standalone/jar/tests.jar").toAbsolutePath().toString());
		path.add(jar.toString());
		var result = Request.builder() //
				.setTool(new Java()) //
				.setProject("standalone") //
				.addArguments("--show-version") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("--class-path", String.join(File.pathSeparator, path)) //
				.addArguments("org.junit.platform.console.ConsoleLauncher") //
				.addArguments("execute") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--fail-if-no-tests") //
				.build() //
				.run(false);

		assertEquals(1, result.getExitCode(), () -> getExitCodeMessage(result));
	}

	private static String getExitCodeMessage(Result result) {
		return "Exit codes don't match. Stdout:\n" + result.getOutput("out") + //
				"\n\nStderr:\n" + result.getOutput("err") + "\n";
	}

	/**
	 * Special override of class {@link Java} to resolve against a different {@code JAVA_HOME}.
	 */
	private static Java getJava8() {
		Path java8Home = Helper.getJavaHome("8").orElseThrow(TestAbortedException::new);
		return new Java() {
			@Override
			public Path getHome() {
				return java8Home;
			}

			@Override
			public String getVersion() {
				return "8";
			}
		};
	}
}
