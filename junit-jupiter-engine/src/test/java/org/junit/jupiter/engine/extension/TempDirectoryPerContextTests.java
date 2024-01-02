/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Supplier;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirFactory;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Events;

/**
 * Integration tests for the legacy behavior of the {@link TempDirectory}
 * extension to create a single temp directory per context, i.e. test class or
 * method.
 *
 * @since 5.4
 */
@DisplayName("TempDirectory extension (per context)")
class TempDirectoryPerContextTests extends AbstractJupiterTestEngineTests {

	@Override
	protected EngineExecutionResults executeTestsForClass(Class<?> testClass) {
		return executeTests(requestBuilder(testClass).build());
	}

	@SuppressWarnings("deprecation")
	private static LauncherDiscoveryRequestBuilder requestBuilder(Class<?> testClass) {
		return request() //
				.selectors(selectClass(testClass)) //
				.configurationParameter(TempDir.SCOPE_PROPERTY_NAME, TempDirectory.Scope.PER_CONTEXT.name());
	}

	@BeforeEach
	@AfterEach
	void resetStaticVariables() {
		BaseSharedTempDirFieldInjectionTestCase.staticTempDir = null;
		BaseSharedTempDirParameterInjectionTestCase.tempDir = null;
		BaseSeparateTempDirsFieldInjectionTestCase.tempDirs.clear();
		BaseSeparateTempDirsParameterInjectionTestCase.tempDirs.clear();
	}

	@Test
	@DisplayName("does not prevent constructor parameter resolution")
	void tempDirectoryDoesNotPreventConstructorParameterResolution() {
		executeTestsForClass(TempDirectoryDoesNotPreventConstructorParameterResolutionTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Test
	@DisplayName("does not prevent user from deleting the temp dir within a test")
	void tempDirectoryDoesNotPreventUserFromDeletingTempDir() {
		executeTestsForClass(UserTempDirectoryDeletionDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Test
	@DisplayName("is capable of removing a read-only file")
	void nonWritableFileDoesNotCauseFailure() {
		executeTestsForClass(NonWritableFileDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Test
	@DisplayName("is capable of removing non-executable, non-writable, or non-readable directories and folders")
	void nonMintPermissionsContentDoesNotCauseFailure() {
		executeTestsForClass(NonMintPermissionContentInTempDirectoryDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(13).succeeded(13));
	}

	@Test
	@DisplayName("is capable of removing a directory when its permissions have been changed")
	void nonMintPermissionsDoNotCauseFailure() {
		executeTestsForClass(NonMintTempDirectoryPermissionsDoNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(42).succeeded(42));
	}

	@Test
	@DisplayName("is capable of removing a read-only file in a read-only dir")
	void readOnlyFileInReadOnlyDirDoesNotCauseFailure() {
		executeTestsForClass(ReadOnlyFileInReadOnlyDirDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Test
	@DisplayName("is capable of removing a read-only file in a dir in a read-only dir")
	void readOnlyFileInNestedReadOnlyDirDoesNotCauseFailure() {
		executeTestsForClass(ReadOnlyFileInDirInReadOnlyDirDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Test
	@DisplayName("can be used via instance field inside nested test classes")
	void canBeUsedViaInstanceFieldInsideNestedTestClasses() {
		executeTestsForClass(TempDirUsageInsideNestedClassesTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(3).succeeded(3));
	}

	@Test
	@DisplayName("can be used via static field inside nested test classes")
	void canBeUsedViaStaticFieldInsideNestedTestClasses() {
		executeTestsForClass(StaticTempDirUsageInsideNestedClassTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(2).succeeded(2));
	}

	@Test
	@DisplayName("resolves java.io.File injection type")
	void resolvesFileInstances() {
		executeTestsForClass(FileInjectionTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Nested
	@DisplayName("resolves shared temp dir")
	@TestMethodOrder(OrderAnnotation.class)
	class SharedTempDir {

		@Test
		@DisplayName("when @TempDir is used on static field")
		@Order(10)
		void resolvesSharedTempDirWhenAnnotationIsUsedOnStaticField() {
			assertSharedTempDirForFieldInjection(AnnotationOnStaticFieldTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on static field and @BeforeAll method parameter")
		@Order(11)
		void resolvesSharedTempDirWhenAnnotationIsUsedOnStaticFieldAndBeforeAllMethodParameter() {
			assertSharedTempDirForFieldInjection(AnnotationOnStaticFieldAndBeforeAllMethodParameterTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on instance field and @BeforeAll method parameter")
		@Order(14)
		void resolvesSharedTempDirWhenAnnotationIsUsedOnInstanceFieldAndBeforeAllMethodParameter() {
			assertSharedTempDirForFieldInjection(AnnotationOnInstanceFieldAndBeforeAllMethodParameterTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on instance field and @BeforeAll method parameter with @TestInstance(PER_CLASS)")
		@Order(15)
		void resolvesSharedTempDirWhenAnnotationIsUsedOnInstanceFieldAndBeforeAllMethodParameterWithTestInstancePerClass() {
			assertSharedTempDirForFieldInjection(
				AnnotationOnInstanceFieldAndBeforeAllMethodParameterWithTestInstancePerClassTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeAll method parameter")
		@Order(23)
		void resolvesSharedTempDirWhenAnnotationIsUsedOnBeforeAllMethodParameter() {
			assertSharedTempDirForParameterInjection(AnnotationOnBeforeAllMethodParameterTestCase.class);
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeAll method parameter with @TestInstance(PER_CLASS)")
		@Order(24)
		void resolvesSharedTempDirWhenAnnotationIsUsedOnBeforeAllMethodParameterWithTestInstancePerClass() {
			assertSharedTempDirForParameterInjection(
				AnnotationOnBeforeAllMethodParameterWithTestInstancePerClassTestCase.class);
		}

		private void assertSharedTempDirForFieldInjection(
				Class<? extends BaseSharedTempDirFieldInjectionTestCase> testClass) {

			assertSharedTempDirForParameterInjection(testClass,
				() -> BaseSharedTempDirFieldInjectionTestCase.staticTempDir);
		}

		private void assertSharedTempDirForParameterInjection(
				Class<? extends BaseSharedTempDirParameterInjectionTestCase> testClass) {

			assertSharedTempDirForParameterInjection(testClass,
				() -> BaseSharedTempDirParameterInjectionTestCase.tempDir);
		}

		private void assertSharedTempDirForParameterInjection(Class<?> testClass, Supplier<Path> staticTempDir) {
			var results = executeTestsForClass(testClass);

			results.testEvents().assertStatistics(stats -> stats.started(2).failed(0).succeeded(2));
			assertThat(staticTempDir.get()).isNotNull().doesNotExist();
		}

	}

	@Nested
	@DisplayName("resolves separate temp dirs")
	@TestMethodOrder(OrderAnnotation.class)
	class SeparateTempDirs {

		@Test
		@DisplayName("when @TempDir is used on instance field")
		@Order(11)
		void resolvesSeparateTempDirWhenAnnotationIsUsedOnInstanceField() {
			assertSeparateTempDirsForFieldInjection(
				SeparateTempDirsWhenUsedOnForEachLifecycleMethodsFieldInjectionTestCase.class);
			assertThat(BaseSeparateTempDirsFieldInjectionTestCase.tempDirs.getFirst()).doesNotExist();
			assertThat(BaseSeparateTempDirsFieldInjectionTestCase.tempDirs.getLast()).doesNotExist();
		}

		@Test
		@DisplayName("when @TempDir is used on instance field with @TestInstance(PER_CLASS)")
		@Order(12)
		void resolvesSeparateTempDirWhenAnnotationIsUsedOnInstanceFieldWithTestInstancePerClass() {
			assertSeparateTempDirsForFieldInjection(
				SeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClassFieldInjectionTestCase.class);
			assertThat(BaseSeparateTempDirsFieldInjectionTestCase.tempDirs.getFirst()).doesNotExist();
			assertThat(BaseSeparateTempDirsFieldInjectionTestCase.tempDirs.getLast()).doesNotExist();
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeEach/@AfterEach method parameters")
		@Order(21)
		void resolvesSeparateTempDirsWhenUsedOnForEachLifecycleMethods() {
			assertSeparateTempDirsForParameterInjection(
				SeparateTempDirsWhenUsedOnForEachLifecycleMethodsParameterInjectionTestCase.class);
			assertThat(BaseSeparateTempDirsParameterInjectionTestCase.tempDirs.getFirst()).doesNotExist();
			assertThat(BaseSeparateTempDirsParameterInjectionTestCase.tempDirs.getLast()).doesNotExist();
		}

		@Test
		@DisplayName("when @TempDir is used on @BeforeEach/@AfterEach method parameters with @TestInstance(PER_CLASS)")
		@Order(22)
		void resolvesSeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClass() {
			assertSeparateTempDirsForParameterInjection(
				SeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClassParameterInjectionTestCase.class);
			assertThat(BaseSeparateTempDirsParameterInjectionTestCase.tempDirs.getFirst()).doesNotExist();
			assertThat(BaseSeparateTempDirsParameterInjectionTestCase.tempDirs.getLast()).doesNotExist();
		}

		@Test
		@DisplayName("for @AfterAll method parameter when @TempDir is not used on constructor or @BeforeAll method parameter")
		@Order(31)
		void resolvesSeparateTempDirWhenAnnotationIsUsedOnAfterAllMethodParameterOnly() {
			var results = executeTestsForClass(AnnotationOnAfterAllMethodParameterTestCase.class);

			results.testEvents().assertStatistics(stats -> stats.started(1).failed(0).succeeded(1));
			assertThat(AnnotationOnAfterAllMethodParameterTestCase.firstTempDir).isNotNull().doesNotExist();
			assertThat(AnnotationOnAfterAllMethodParameterTestCase.secondTempDir).isNotNull().doesNotExist();
		}

	}

	@Nested
	@DisplayName("supports default factory")
	@TestMethodOrder(OrderAnnotation.class)
	class DefaultFactory {

		private Events executeTestsForClassWithDefaultFactory(Class<?> testClass,
				Class<? extends TempDirFactory> factoryClass) {
			return TempDirectoryPerContextTests.super.executeTests(requestBuilder(testClass) //
					.configurationParameter(TempDir.DEFAULT_FACTORY_PROPERTY_NAME, factoryClass.getName()) //
					.build()).testEvents();
		}

		@Test
		@DisplayName("set to Jupiter's default")
		void supportsStandardDefaultFactory() {
			executeTestsForClassWithDefaultFactory(StandardDefaultFactoryTestCase.class, TempDirFactory.Standard.class) //
					.assertStatistics(stats -> stats.started(1).succeeded(1));
		}

		@Test
		@DisplayName("set to custom factory")
		void supportsCustomDefaultFactory() {
			executeTestsForClassWithDefaultFactory(NonStandardDefaultFactoryTestCase.class, Factory.class) //
					.assertStatistics(stats -> stats.started(1).succeeded(1));
		}

		private static class Factory implements TempDirFactory {

			@Override
			public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext)
					throws Exception {
				return Files.createTempDirectory("junit");
			}
		}

	}

	@Nested
	@DisplayName("reports failure")
	@TestMethodOrder(OrderAnnotation.class)
	class Failures {

		@Test
		@DisplayName("when @TempDir is used on static field of an unsupported type")
		@Order(20)
		void onlySupportsStaticFieldsOfTypePathAndFile() {
			var results = executeTestsForClass(AnnotationOnStaticFieldWithUnsupportedTypeTestCase.class);

			assertSingleFailedContainer(results, ExtensionConfigurationException.class,
				"Can only resolve @TempDir field of type java.nio.file.Path or java.io.File");
		}

		@Test
		@DisplayName("when @TempDir is used on instance field of an unsupported type")
		@Order(21)
		void onlySupportsInstanceFieldsOfTypePathAndFile() {
			var results = executeTestsForClass(AnnotationOnInstanceFieldWithUnsupportedTypeTestCase.class);

			assertSingleFailedTest(results, ExtensionConfigurationException.class,
				"Can only resolve @TempDir field of type java.nio.file.Path or java.io.File");
		}

		@Test
		@DisplayName("when @TempDir is used on parameter of an unsupported type")
		@Order(22)
		void onlySupportsParametersOfTypePathAndFile() {
			var results = executeTestsForClass(InvalidTestCase.class);

			// @formatter:off
			TempDirectoryPerContextTests.assertSingleFailedTest(results,
				instanceOf(ParameterResolutionException.class),
				message(m -> m.matches("Failed to resolve parameter \\[java.lang.String .+] in method \\[.+]: .+")),
				cause(
					instanceOf(ExtensionConfigurationException.class),
					message("Can only resolve @TempDir parameter of type java.nio.file.Path or java.io.File but was: java.lang.String")));
			// @formatter:on
		}

		@Test
		@DisplayName("when @TempDir is used on constructor parameter")
		@Order(30)
		void doesNotSupportTempDirAnnotationOnConstructorParameter() {
			var results = executeTestsForClass(AnnotationOnConstructorParameterTestCase.class);

			assertSingleFailedTest(results, ParameterResolutionException.class,
				"@TempDir is not supported on constructor parameters. Please use field injection instead.");
		}

		@Test
		@DisplayName("when @TempDir is used on constructor parameter with @TestInstance(PER_CLASS)")
		@Order(31)
		void doesNotSupportTempDirAnnotationOnConstructorParameterWithTestInstancePerClass() {
			var results = executeTestsForClass(AnnotationOnConstructorParameterWithTestInstancePerClassTestCase.class);

			assertSingleFailedContainer(results, ParameterResolutionException.class,
				"@TempDir is not supported on constructor parameters. Please use field injection instead.");
		}

		@Test
		@DisplayName("when @TempDir factory is not Standard")
		@Order(32)
		void onlySupportsStandardTempDirFactory() {
			var results = executeTestsForClass(NonStandardFactoryTestCase.class);

			// @formatter:off
			TempDirectoryPerContextTests.assertSingleFailedTest(results,
					instanceOf(ParameterResolutionException.class),
					message(m -> m.matches("Failed to resolve parameter \\[.+] in method \\[.+]: .+")),
					cause(
							instanceOf(ExtensionConfigurationException.class),
							message("Custom @TempDir factory is not supported with junit.jupiter.tempdir.scope=per_context. "
									+ "Use junit.jupiter.tempdir.factory.default instead.")));
			// @formatter:on
		}

	}

	@Nested
	@DisplayName("supports @TempDir")
	@TestMethodOrder(OrderAnnotation.class)
	class PrivateFields {

		@Test
		@DisplayName("on private static field")
		@Order(10)
		void supportsPrivateInstanceFields() {
			executeTestsForClass(AnnotationOnPrivateStaticFieldTestCase.class).testEvents()//
					.assertStatistics(stats -> stats.started(1).succeeded(1));
		}

		@Test
		@DisplayName("on private instance field")
		@Order(11)
		void supportsPrivateStaticFields() {
			executeTestsForClass(AnnotationOnPrivateInstanceFieldTestCase.class).testEvents()//
					.assertStatistics(stats -> stats.started(1).succeeded(1));
		}

	}

	private static void assertSingleFailedContainer(EngineExecutionResults results, Class<? extends Throwable> clazz,
			String message) {

		assertSingleFailedContainer(results, instanceOf(clazz), message(actual -> actual.contains(message)));
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private static void assertSingleFailedContainer(EngineExecutionResults results,
			Condition<Throwable>... conditions) {

		results.containerEvents()//
				.assertStatistics(stats -> stats.started(2).failed(1).succeeded(1))//
				.assertThatEvents().haveExactly(1, finishedWithFailure(conditions));
	}

	private static void assertSingleFailedTest(EngineExecutionResults results, Class<? extends Throwable> clazz,
			String message) {

		assertSingleFailedTest(results, instanceOf(clazz), message(actual -> actual.contains(message)));
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private static void assertSingleFailedTest(EngineExecutionResults results, Condition<Throwable>... conditions) {
		results.testEvents().assertStatistics(stats -> stats.started(1).failed(1).succeeded(0));
		results.testEvents().assertThatEvents().haveExactly(1, finishedWithFailure(conditions));
	}

	private void assertSeparateTempDirsForFieldInjection(
			Class<? extends BaseSeparateTempDirsFieldInjectionTestCase> testClass) {

		assertResolvesSeparateTempDirs(testClass, BaseSeparateTempDirsFieldInjectionTestCase.tempDirs);
	}

	private void assertSeparateTempDirsForParameterInjection(
			Class<? extends BaseSeparateTempDirsParameterInjectionTestCase> testClass) {

		assertResolvesSeparateTempDirs(testClass, BaseSeparateTempDirsParameterInjectionTestCase.tempDirs);
	}

	private void assertResolvesSeparateTempDirs(Class<?> testClass, Deque<Path> tempDirs) {
		var results = executeTestsForClass(testClass);

		results.testEvents().assertStatistics(stats -> stats.started(2).failed(0).succeeded(2));
		assertThat(tempDirs).hasSize(2);
	}

	// -------------------------------------------------------------------------

	static class BaseSharedTempDirFieldInjectionTestCase {

		static Path staticTempDir;

		@TempDir
		Path tempDir;

		@BeforeEach
		void beforeEach(@TempDir Path tempDir) {
			if (BaseSharedTempDirFieldInjectionTestCase.staticTempDir != null) {
				assertSame(BaseSharedTempDirFieldInjectionTestCase.staticTempDir, tempDir);
			}
			else {
				BaseSharedTempDirFieldInjectionTestCase.staticTempDir = tempDir;
			}
			check(tempDir);
		}

		@Test
		void test1(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@Test
		void test2(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@AfterEach
		void afterEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		void check(Path tempDir) {
			assertThat(BaseSharedTempDirFieldInjectionTestCase.staticTempDir)//
					.isNotNull()//
					.isSameAs(tempDir)//
					.isSameAs(this.tempDir);
			assertTrue(Files.exists(tempDir));
		}

	}

	static class AnnotationOnStaticFieldTestCase extends BaseSharedTempDirFieldInjectionTestCase {

		@TempDir
		static Path staticTempPath;

		@TempDir
		static File staticTempFile;

		@Override
		void check(Path tempDir) {
			assertThat(BaseSharedTempDirFieldInjectionTestCase.staticTempDir)//
					.isNotNull()//
					.isSameAs(AnnotationOnStaticFieldTestCase.staticTempPath)//
					.isSameAs(tempDir)//
					.isSameAs(this.tempDir);
			assertTrue(Files.exists(tempDir));
		}

	}

	static class AnnotationOnStaticFieldAndBeforeAllMethodParameterTestCase extends AnnotationOnStaticFieldTestCase {

		@BeforeAll
		static void beforeAll(@TempDir Path tempDir) {
			assertThat(BaseSharedTempDirFieldInjectionTestCase.staticTempDir).isNull();
			BaseSharedTempDirFieldInjectionTestCase.staticTempDir = tempDir;
			assertThat(AnnotationOnStaticFieldTestCase.staticTempFile).isNotNull();
			assertThat(AnnotationOnStaticFieldTestCase.staticTempPath)//
					.isNotNull()//
					.isSameAs(tempDir);
			assertThat(AnnotationOnStaticFieldTestCase.staticTempFile.toPath().toAbsolutePath())//
					.isEqualTo(AnnotationOnStaticFieldTestCase.staticTempPath.toAbsolutePath());
			assertTrue(Files.exists(tempDir));
		}

	}

	static class AnnotationOnInstanceFieldAndBeforeAllMethodParameterTestCase
			extends BaseSharedTempDirFieldInjectionTestCase {

		@BeforeAll
		static void beforeAll(@TempDir Path tempDir) {
			assertThat(BaseSharedTempDirFieldInjectionTestCase.staticTempDir).isNull();
			BaseSharedTempDirFieldInjectionTestCase.staticTempDir = tempDir;
			assertTrue(Files.exists(tempDir));
		}

	}

	@TestInstance(PER_CLASS)
	static class AnnotationOnInstanceFieldAndBeforeAllMethodParameterWithTestInstancePerClassTestCase
			extends AnnotationOnInstanceFieldAndBeforeAllMethodParameterTestCase {

	}

	static class AnnotationOnPrivateInstanceFieldTestCase {

		@SuppressWarnings("unused")
		@TempDir
		private Path tempDir;

		@Test
		void test() {
			assertTrue(Files.exists(tempDir));
		}

	}

	static class AnnotationOnPrivateStaticFieldTestCase {

		@SuppressWarnings("unused")
		@TempDir
		private static Path tempDir;

		@Test
		void test() {
			assertTrue(Files.exists(tempDir));
		}

	}

	static class AnnotationOnStaticFieldWithUnsupportedTypeTestCase {

		@SuppressWarnings("unused")
		@TempDir
		static String tempDir;

		@Test
		void test1() {
		}

	}

	static class AnnotationOnInstanceFieldWithUnsupportedTypeTestCase {

		@SuppressWarnings("unused")
		@TempDir
		String tempDir;

		@Test
		void test1() {
		}

	}

	static class BaseSharedTempDirParameterInjectionTestCase {

		static Path tempDir;

		@BeforeEach
		void beforeEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		@Test
		void test1(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@Test
		void test2(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@AfterEach
		void afterEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		static void check(Path tempDir) {
			assertThat(BaseSharedTempDirParameterInjectionTestCase.tempDir).isNotNull().isSameAs(tempDir);
			assertTrue(Files.exists(tempDir));
		}

	}

	static class AnnotationOnConstructorParameterTestCase {

		AnnotationOnConstructorParameterTestCase(@SuppressWarnings("unused") @TempDir Path tempDir) {
			// never called
		}

		@Test
		void test() {
			// never called
		}

	}

	@TestInstance(PER_CLASS)
	static class AnnotationOnConstructorParameterWithTestInstancePerClassTestCase
			extends AnnotationOnConstructorParameterTestCase {

		AnnotationOnConstructorParameterWithTestInstancePerClassTestCase(@TempDir Path tempDir) {
			super(tempDir);
		}
	}

	static class NonStandardFactoryTestCase {

		@Test
		void test(@SuppressWarnings("unused") @TempDir(factory = Factory.class) Path tempDir) {
			// never called
		}

		private static class Factory implements TempDirFactory {

			@Override
			public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext)
					throws Exception {
				return Files.createTempDirectory("junit");
			}
		}

	}

	static class StandardDefaultFactoryTestCase {

		@Test
		void test(@TempDir Path tempDir1, @TempDir Path tempDir2) {
			assertSame(tempDir1, tempDir2);
		}

	}

	static class NonStandardDefaultFactoryTestCase {

		@Test
		void test(@TempDir Path tempDir1, @TempDir Path tempDir2) {
			assertSame(tempDir1, tempDir2);
		}

	}

	static class AnnotationOnBeforeAllMethodParameterTestCase extends BaseSharedTempDirParameterInjectionTestCase {

		@BeforeAll
		static void beforeAll(@TempDir Path tempDir) {
			assertThat(BaseSharedTempDirParameterInjectionTestCase.tempDir).isNull();
			BaseSharedTempDirParameterInjectionTestCase.tempDir = tempDir;
			check(tempDir);
		}
	}

	@TestInstance(PER_CLASS)
	static class AnnotationOnBeforeAllMethodParameterWithTestInstancePerClassTestCase
			extends BaseSharedTempDirParameterInjectionTestCase {

		@BeforeAll
		void beforeAll(@TempDir Path tempDir) {
			assertThat(BaseSharedTempDirParameterInjectionTestCase.tempDir).isNull();
			BaseSharedTempDirParameterInjectionTestCase.tempDir = tempDir;
			check(tempDir);
		}
	}

	static class AnnotationOnAfterAllMethodParameterTestCase {

		static Path firstTempDir = null;
		static Path secondTempDir = null;

		@Test
		void test(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			assertThat(firstTempDir).isNull();
			firstTempDir = tempDir;
			writeFile(tempDir, testInfo);
		}

		@AfterAll
		static void afterAll(@TempDir Path tempDir) {
			assertThat(firstTempDir).isNotNull();
			assertNotEquals(firstTempDir, tempDir);
			secondTempDir = tempDir;
		}
	}

	static class BaseSeparateTempDirsFieldInjectionTestCase {

		static final Deque<Path> tempDirs = new LinkedList<>();

		@TempDir
		Path tempDir;

		@BeforeEach
		void beforeEach(@TempDir Path tempDir) {
			for (Path dir : tempDirs) {
				assertThat(dir).doesNotExist();
			}
			assertThat(tempDirs).doesNotContain(tempDir);
			tempDirs.add(tempDir);
			check(tempDir);
		}

		@Test
		void test1(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@Test
		void test2(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@AfterEach
		void afterEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		void check(Path tempDir) {
			assertThat(tempDirs.getLast())//
					.isNotNull()//
					.isSameAs(tempDir)//
					.isSameAs(this.tempDir);
			assertTrue(Files.exists(tempDir));
		}

	}

	static class SeparateTempDirsWhenUsedOnForEachLifecycleMethodsFieldInjectionTestCase
			extends BaseSeparateTempDirsFieldInjectionTestCase {
	}

	@TestInstance(PER_CLASS)
	static class SeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClassFieldInjectionTestCase
			extends SeparateTempDirsWhenUsedOnForEachLifecycleMethodsFieldInjectionTestCase {
	}

	static class BaseSeparateTempDirsParameterInjectionTestCase {

		static final Deque<Path> tempDirs = new LinkedList<>();

		@BeforeEach
		void beforeEach(@TempDir Path tempDir) {
			for (Path dir : tempDirs) {
				assertThat(dir).doesNotExist();
			}
			assertThat(tempDirs).doesNotContain(tempDir);
			tempDirs.add(tempDir);
			check(tempDir);
		}

		@Test
		void test1(@TempDir Path tempDir, TestInfo testInfo) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@Test
		void test2(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
			check(tempDir);
			writeFile(tempDir, testInfo);
		}

		@AfterEach
		void afterEach(@TempDir Path tempDir) {
			check(tempDir);
		}

		void check(Path tempDir) {
			assertSame(tempDirs.getLast(), tempDir);
			assertTrue(Files.exists(tempDir));
		}
	}

	static class SeparateTempDirsWhenUsedOnForEachLifecycleMethodsParameterInjectionTestCase
			extends BaseSeparateTempDirsParameterInjectionTestCase {
	}

	@TestInstance(PER_CLASS)
	static class SeparateTempDirsWhenUsedOnForEachLifecycleMethodsWithTestInstancePerClassParameterInjectionTestCase
			extends SeparateTempDirsWhenUsedOnForEachLifecycleMethodsParameterInjectionTestCase {
	}

	static class InvalidTestCase {

		@Test
		void wrongParameterType(@SuppressWarnings("unused") @TempDir String ignored) {
			fail("this should never be called");
		}
	}

	static class FileInjectionTestCase {

		@TempDir
		File fileTempDir;

		@TempDir
		Path pathTempDir;

		@Test
		void checkFile(@TempDir File tempDir, @TempDir Path ref) {
			assertFileAndPathAreEqual(tempDir, ref);
			assertFileAndPathAreEqual(this.fileTempDir, this.pathTempDir);
		}

		private void assertFileAndPathAreEqual(File tempDir, Path ref) {
			Path path = tempDir.toPath();
			assertEquals(ref.toAbsolutePath(), path.toAbsolutePath());
			assertTrue(Files.exists(path));
		}

	}

	private static void writeFile(Path tempDir, TestInfo testInfo) throws IOException {
		Path file = tempDir.resolve(testInfo.getTestMethod().orElseThrow().getName() + ".txt");
		Files.write(file, testInfo.getDisplayName().getBytes());
	}

	// https://github.com/junit-team/junit5/issues/1748
	static class TempDirectoryDoesNotPreventConstructorParameterResolutionTestCase {

		TempDirectoryDoesNotPreventConstructorParameterResolutionTestCase(TestInfo testInfo) {
			assertNotNull(testInfo);
		}

		@Test
		void test() {
		}

	}

	// https://github.com/junit-team/junit5/issues/1801
	static class UserTempDirectoryDeletionDoesNotCauseFailureTestCase {

		@Test
		void deleteTempDir(@TempDir Path tempDir) throws IOException {
			Files.delete(tempDir);
			assertThat(tempDir).doesNotExist();
		}

	}

	// https://github.com/junit-team/junit5/issues/2046
	static class NonWritableFileDoesNotCauseFailureTestCase {

		@Test
		void createReadonlyFile(@TempDir Path tempDir) throws IOException {
			// Removal of setWritable(false) files might fail (e.g. for Windows)
			// The test verifies that @TempDir is capable of removing of such files
			var path = Files.write(tempDir.resolve("test.txt"), new byte[0]);
			assumeTrue(path.toFile().setWritable(false),
				() -> "Unable to set file " + path + " readonly via .toFile().setWritable(false)");
		}

	}

	// https://github.com/junit-team/junit5/issues/2171
	static class ReadOnlyFileInReadOnlyDirDoesNotCauseFailureTestCase {

		@Test
		void createReadOnlyFileInReadOnlyDir(@TempDir File tempDir) throws IOException {
			File file = tempDir.toPath().resolve("file").toFile();
			assumeTrue(file.createNewFile());
			assumeTrue(makeReadOnly(tempDir));
			assumeTrue(makeReadOnly(file));
		}

	}

	// https://github.com/junit-team/junit5/issues/2171
	static class ReadOnlyFileInDirInReadOnlyDirDoesNotCauseFailureTestCase {

		@Test
		void createReadOnlyFileInReadOnlyDir(@TempDir File tempDir) throws IOException {
			File file = tempDir.toPath().resolve("dir").resolve("file").toFile();
			assumeTrue(file.getParentFile().mkdirs());
			assumeTrue(file.createNewFile());
			assumeTrue(makeReadOnly(tempDir));
			assumeTrue(makeReadOnly(file.getParentFile()));
			assumeTrue(makeReadOnly(file));
		}

	}

	private static boolean makeReadOnly(File file) throws IOException {
		var dos = Files.getFileAttributeView(file.toPath(), DosFileAttributeView.class);
		if (dos != null) {
			dos.setReadOnly(true);
			return true;
		}
		return file.setReadOnly();
	}

	// https://github.com/junit-team/junit5/issues/2609
	@SuppressWarnings("ResultOfMethodCallIgnored")
	static class NonMintPermissionContentInTempDirectoryDoesNotCauseFailureTestCase {

		@Test
		void createFile(@TempDir Path tempDir) throws IOException {
			Files.createFile(tempDir.resolve("test-file.txt")).toFile();
		}

		@Test
		void createFolder(@TempDir Path tempDir) throws IOException {
			Files.createFile(tempDir.resolve("test-file.txt")).toFile();
		}

		@Test
		void createNonWritableFile(@TempDir Path tempDir) throws IOException {
			Files.createFile(tempDir.resolve("test-file.txt")).toFile().setWritable(false);
		}

		@Test
		void createNonReadableFile(@TempDir Path tempDir) throws IOException {
			Files.createFile(tempDir.resolve("test-file.txt")).toFile().setReadable(false);
		}

		@Test
		void createNonWritableDirectory(@TempDir Path tempDir) throws IOException {
			Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setWritable(false);
		}

		@Test
		void createNonReadableDirectory(@TempDir Path tempDir) throws IOException {
			Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setReadable(false);
		}

		@Test
		void createNonExecutableDirectory(@TempDir Path tempDir) throws IOException {
			Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setExecutable(false);
		}

		@Test
		void createNonEmptyNonWritableDirectory(@TempDir Path tempDir) throws IOException {
			Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
			Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
			subDir.toFile().setWritable(false);
		}

		@Test
		void createNonEmptyNonReadableDirectory(@TempDir Path tempDir) throws IOException {
			Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
			Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
			subDir.toFile().setReadable(false);
		}

		@Test
		void createNonEmptyNonExecutableDirectory(@TempDir Path tempDir) throws IOException {
			Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
			Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
			subDir.toFile().setExecutable(false);
		}

		@Test
		void createNonEmptyDirectory(@TempDir Path tempDir) throws IOException {
			Files.createDirectory(tempDir.resolve("test-sub-dir"));
			Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
		}

		@Test
		void createNonEmptyDirectoryWithNonWritableFile(@TempDir Path tempDir) throws IOException {
			Files.createDirectory(tempDir.resolve("test-sub-dir"));
			Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setWritable(false);
		}

		@Test
		void createNonEmptyDirectoryWithNonReadableFile(@TempDir Path tempDir) throws IOException {
			Files.createDirectory(tempDir.resolve("test-sub-dir"));
			Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setReadable(false);
		}
	}

	// https://github.com/junit-team/junit5/issues/2609
	@SuppressWarnings("ResultOfMethodCallIgnored")
	static class NonMintTempDirectoryPermissionsDoNotCauseFailureTestCase {

		@Nested
		class NonWritable {

			@Test
			void makeEmptyTempDirectoryNonWritable(@TempDir Path tempDir) {
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithFileNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt"));
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithEmptyFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonWritableFileNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt")).toFile().setWritable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonReadableFileNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt")).toFile().setReadable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonWritableFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setWritable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonReadableFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setReadable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonExecutableFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setExecutable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonReadableFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setReadable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonWritableFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setWritable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonExecutableFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setExecutable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderNonWritable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderContainingNonWritableFileNonWritable(@TempDir Path tempDir)
					throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setWritable(false);
				tempDir.toFile().setWritable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderContainingNonReadableFileNonWritable(@TempDir Path tempDir)
					throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setReadable(false);
				tempDir.toFile().setWritable(false);
			}
		}

		@Nested
		class NonReadable {

			@Test
			void makeEmptyTempDirectoryNonReadable(@TempDir Path tempDir) {
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithFileNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt"));
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithEmptyFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonWritableFileNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt")).toFile().setWritable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonReadableFileNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt")).toFile().setReadable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonWritableFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setWritable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonReadableFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setReadable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonExecutableFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setExecutable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonWritableFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setWritable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonReadableFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setReadable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonExecutableFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setExecutable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderNonReadable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderContainingNonWritableFileNonReadable(@TempDir Path tempDir)
					throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setWritable(false);
				tempDir.toFile().setReadable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderContainingNonReadableFileNonReadable(@TempDir Path tempDir)
					throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setReadable(false);
				tempDir.toFile().setReadable(false);
			}
		}

		@Nested
		class NonExecutable {

			@Test
			void makeEmptyTempDirectoryNonExecutable(@TempDir Path tempDir) {
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithFileNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt"));
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithEmptyFolderNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonWritableFileNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt")).toFile().setWritable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonReadableFileNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createFile(tempDir.resolve("test-file.txt")).toFile().setReadable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonWritableFolderNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setWritable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonReadableFolderNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setReadable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonExecutableFolderNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir")).toFile().setExecutable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonWritableFolderNonExecutable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setWritable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonReadableFolderNonExecutable(@TempDir Path tempDir) throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setReadable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyNonExecutableFolderNonExecutable(@TempDir Path tempDir)
					throws IOException {
				Path subDir = Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				subDir.toFile().setExecutable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderNonExecutable(@TempDir Path tempDir) throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt"));
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderContainingNonWritableFileNonExecutable(@TempDir Path tempDir)
					throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setWritable(false);
				tempDir.toFile().setExecutable(false);
			}

			@Test
			void makeTempDirectoryWithNonEmptyFolderContainingNonReadableFileNonExecutable(@TempDir Path tempDir)
					throws IOException {
				Files.createDirectory(tempDir.resolve("test-sub-dir"));
				Files.createFile(tempDir.resolve("test-sub-dir/test-file.txt")).toFile().setReadable(false);
				tempDir.toFile().setExecutable(false);
			}
		}
	}

	// https://github.com/junit-team/junit5/issues/2079
	static class TempDirUsageInsideNestedClassesTestCase {

		@TempDir
		File tempDir;

		@Test
		void topLevel() {
			assertNotNull(tempDir);
			assertTrue(tempDir.exists());
		}

		@Nested
		class NestedTestClass {

			@Test
			void nested() {
				assertNotNull(tempDir);
				assertTrue(tempDir.exists());
			}

			@Nested
			class EvenDeeperNestedTestClass {

				@Test
				void deeplyNested() {
					assertNotNull(tempDir);
					assertTrue(tempDir.exists());
				}
			}
		}
	}

	static class StaticTempDirUsageInsideNestedClassTestCase {

		@TempDir
		static File tempDir;

		static File initialTempDir;

		@Test
		void topLevel() {
			assertNotNull(tempDir);
			assertTrue(tempDir.exists());
			initialTempDir = tempDir;
		}

		@Nested
		class NestedTestClass {

			@Test
			void nested() {
				assertNotNull(tempDir);
				assertTrue(tempDir.exists());
				assertSame(initialTempDir, tempDir);
			}
		}
	}

}
