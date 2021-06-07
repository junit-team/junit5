/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * Integration tests for the {@link TempDirectory} extension.
 *
 * @since 5.4
 */
@DisplayName("TempDirectory extension")
class TempDirectoryTests extends AbstractJupiterTestEngineTests {

	@BeforeEach
	@AfterEach
	void resetStaticVariables() {
		BaseSharedTempDirFieldInjectionTestCase.staticTempDir = null;
		BaseSharedTempDirParameterInjectionTestCase.tempDir = null;
		BaseSeparateTempDirsParameterInjectionTestCase.tempDirs.clear();
	}

	@Test
	void supportsMultipleTempDirs() {
		executeTestsForClass(MultipleTempDirsTestCase.class).allEvents()//
				.assertStatistics(stats -> stats.started(3).succeeded(3));
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
	@DisplayName("is capable of removal of a read-only file")
	void nonWritableFileDoesNotCauseFailure() {
		executeTestsForClass(NonWritableFileDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	@DisplayName("is capable of removal of a read-only file in a read-only dir")
	void readOnlyFileInReadOnlyDirDoesNotCauseFailure() {
		executeTestsForClass(ReadOnlyFileInReadOnlyDirDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	@DisplayName("is capable of removal of a read-only file in a dir in a read-only dir")
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

	@Nested
	@DisplayName("resolves shared temp dir")
	@TestMethodOrder(OrderAnnotation.class)
	class SharedTempDir {

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
	@DisplayName("reports failure")
	@TestMethodOrder(OrderAnnotation.class)
	class Failures {

		@Test
		@DisplayName("when @TempDir is used on private static field")
		@Order(10)
		void onlySupportsNonPrivateInstanceFields() {
			var results = executeTestsForClass(AnnotationOnPrivateStaticFieldTestCase.class);

			assertSingleFailedContainer(results, ExtensionConfigurationException.class, "must not be private");
		}

		@Test
		@DisplayName("when @TempDir is used on private instance field")
		@Order(11)
		void onlySupportsNonPrivateStaticFields() {
			var results = executeTestsForClass(AnnotationOnPrivateInstanceFieldTestCase.class);

			assertSingleFailedTest(results, ExtensionConfigurationException.class, "must not be private");
		}

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
			TempDirectoryTests.assertSingleFailedTest(results,
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

	}

	@Nested
	@DisplayName("reports illegal usage")
	class IllegalUsage {

		@Test
		@DisplayName("when @TempDir with same identifier is used on multiple method parameters")
		void doesNotSupportMultipleTempDirAnnotationsWithSameIdentifierOnOneMethod() throws NoSuchMethodException {
			var testClass = IllegalUsageTestCase.MultipleMethodParameters.class;
			var results = executeTestsForClass(testClass);
			var method = testClass.getDeclaredMethod("test", Path.class, File.class);

			assertSingleFailedTest(results, ParameterResolutionException.class,
				"The same @TempDir was declared on multiple parameters of the following method: " + method
						+ ". Please specify distinct identifiers to create different temporary directories via @TempDir(...) or remove the duplicate parameter.");
		}

		@Test
		@DisplayName("when @TempDir with same identifier is used on multiple instance fields")
		void doesNotSupportTempDirAnnotationsWithSameIdentifierOnMultipleInstanceFields() throws NoSuchFieldException {
			var testClass = IllegalUsageTestCase.MultipleInstanceFields.class;
			var results = executeTestsForClass(testClass);
			var firstField = IllegalUsageTestCase.InstanceField.class.getDeclaredField("path");
			var secondField = testClass.getDeclaredField("file");

			assertSingleFailedTest(results, JUnitException.class,
				"The same @TempDir was declared in redundant locations: (1) on field " + firstField
						+ " and (2) on field " + secondField
						+ ". Please specify distinct identifiers to create different temporary directories via @TempDir(...) or remove the redundancy.");
		}

		@Test
		@DisplayName("when @TempDir with same identifier is used on instance field and parameter")
		void doesNotSupportTempDirAnnotationsOnFieldAndParameter() throws ReflectiveOperationException {
			var testClass = IllegalUsageTestCase.InstanceFieldAndInstanceParameter.class;
			var results = executeTestsForClass(testClass);
			var field = testClass.getDeclaredField("tempDir");
			var parameter = testClass.getDeclaredMethod("test", Path.class).getParameters()[0];

			assertSingleFailedTest(results, JUnitException.class,
				"The same @TempDir was declared in redundant locations: (1) on field " + field
						+ " and (2) on parameter " + parameter
						+ ". Please specify distinct identifiers to create different temporary directories via @TempDir(...) or remove the redundancy.");
		}

		@Test
		@DisplayName("when @TempDir with same identifier is used on multiple static fields")
		void doesNotSupportTempDirAnnotationsWithSameIdentifierOnMultipleStaticFields() throws NoSuchFieldException {
			var testClass = IllegalUsageTestCase.MultipleStaticFields.class;
			var results = executeTestsForClass(testClass);
			var firstField = IllegalUsageTestCase.StaticField.class.getDeclaredField("path");
			var secondField = testClass.getDeclaredField("file");

			assertSingleFailedContainer(results, JUnitException.class,
				"The same @TempDir was declared in redundant locations: (1) on field " + firstField
						+ " and (2) on field " + secondField
						+ ". Please specify distinct identifiers to create different temporary directories via @TempDir(...) or remove the redundancy.");
		}

		@Test
		@DisplayName("when @TempDir with same identifier is used on multiple static fields")
		void doesNotSupportTempDirAnnotationsWithSameIdentifierOnStaticAndInstanceField() throws NoSuchFieldException {
			var testClass = IllegalUsageTestCase.StaticAndInstanceField.class;
			var results = executeTestsForClass(testClass);
			var firstField = IllegalUsageTestCase.StaticField.class.getDeclaredField("path");
			var secondField = testClass.getDeclaredField("file");

			assertSingleFailedTest(results, JUnitException.class,
				"The same @TempDir was declared in redundant locations: (1) on field " + firstField
						+ " and (2) on field " + secondField
						+ ". Please specify distinct identifiers to create different temporary directories via @TempDir(...) or remove the redundancy.");
		}

		@Test
		@DisplayName("when @TempDir with same identifier is used on static field and parameter")
		void doesNotSupportTempDirAnnotationsOnStaticFieldAndParameterMethod() throws ReflectiveOperationException {
			var testClass = IllegalUsageTestCase.StaticFieldAndParameter.class;
			var results = executeTestsForClass(testClass);
			var field = IllegalUsageTestCase.StaticField.class.getDeclaredField("path");
			var parameter = testClass.getDeclaredMethod("beforeAll", Path.class).getParameters()[0];

			assertSingleFailedContainer(results, JUnitException.class,
				"The same @TempDir was declared in redundant locations: (1) on field " + field
						+ " and (2) on parameter " + parameter
						+ ". Please specify distinct identifiers to create different temporary directories via @TempDir(...) or remove the redundancy.");
		}

		@Test
		@DisplayName("when @TempDir with same identifier is used on instance field and static method parameter")
		void doesNotSupportTempDirAnnotationsOnInstanceFieldAndStaticParameterMethod()
				throws ReflectiveOperationException {
			var testClass = IllegalUsageTestCase.InstanceFieldAndStaticParameter.class;
			var results = executeTestsForClass(testClass);
			var parameter = testClass.getDeclaredMethod("beforeAll", Path.class).getParameters()[0];
			var field = IllegalUsageTestCase.InstanceField.class.getDeclaredField("path");

			assertSingleFailedTest(results, JUnitException.class,
				"The same @TempDir was declared in redundant locations: (1) on parameter " + parameter
						+ " and (2) on field " + field
						+ ". Please specify distinct identifiers to create different temporary directories via @TempDir(...) or remove the redundancy.");
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

	private void assertSeparateTempDirsForParameterInjection(
			Class<? extends BaseSeparateTempDirsParameterInjectionTestCase> testClass) {

		var results = executeTestsForClass(testClass);
		results.testEvents().assertStatistics(stats -> stats.started(2).failed(0).succeeded(2));
		assertThat(BaseSeparateTempDirsParameterInjectionTestCase.tempDirs).hasSize(2);
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

	static class AnnotationOnPrivateInstanceFieldTestCase {

		@SuppressWarnings("unused")
		@TempDir
		private Path tempDir;

		@Test
		void test() {
		}

	}

	static class AnnotationOnPrivateStaticFieldTestCase {

		@SuppressWarnings("unused")
		@TempDir
		private static Path tempDir;

		@Test
		void test() {
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

	@Nested
	@DisplayName("resolves java.io.File injection type")
	class FileAndPathInjection {

		Path path;

		@BeforeEach
		void beforeEach(@TempDir Path path) {
			this.path = path;
		}

		@Test
		@DisplayName("and injected File and Path reference the same temp directory")
		void checkFile(@TempDir File file) {
			assertFileAndPathAreEqual(file, path);
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
			assumeTrue(tempDir.setReadOnly());
			assumeTrue(file.setReadOnly());
		}

	}

	// https://github.com/junit-team/junit5/issues/2171
	static class ReadOnlyFileInDirInReadOnlyDirDoesNotCauseFailureTestCase {

		@Test
		void createReadOnlyFileInReadOnlyDir(@TempDir File tempDir) throws IOException {
			File file = tempDir.toPath().resolve("dir").resolve("file").toFile();
			assumeTrue(file.getParentFile().mkdirs());
			assumeTrue(file.createNewFile());
			assumeTrue(tempDir.setReadOnly());
			assumeTrue(file.getParentFile().setReadOnly());
			assumeTrue(file.setReadOnly());
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

	static class MultipleTempDirsTestCase {

		@TempDir("a")
		static Path A;
		static Path B;

		@BeforeAll
		static void beforeAll(@TempDir("b") Path dir) {
			B = requireNonNull(dir);
			assertNotEquals(A, B);
			assertTrue(Files.exists(A));
			assertTrue(Files.exists(B));
		}

		@TempDir("c")
		Path c;
		static Path C, D, E;

		@BeforeEach
		void beforeEach(@TempDir("d") Path dir) {
			C = requireNonNull(c);
			assertNotEquals(A, C);
			assertNotEquals(B, C);

			D = requireNonNull(dir);
			assertNotEquals(A, D);
			assertNotEquals(B, D);
			assertNotEquals(C, D);

			assertTrue(Files.exists(A));
			assertTrue(Files.exists(B));
			assertTrue(Files.exists(C));
			assertTrue(Files.exists(D));
		}

		@Test
		void test(@TempDir("e") Path dir1, @TempDir("d") Path dir2) {
			assertEquals(D, dir2);

			E = requireNonNull(dir1);
			assertNotEquals(A, E);
			assertNotEquals(B, E);
			assertNotEquals(C, E);
			assertNotEquals(D, E);

			assertTrue(Files.exists(A));
			assertTrue(Files.exists(B));
			assertTrue(Files.exists(C));
			assertTrue(Files.exists(D));
			assertTrue(Files.exists(E));
		}

		@AfterEach
		void afterEach(@TempDir("d") Path dir1, @TempDir("e") Path dir2) {
			assertEquals(D, dir1);
			assertEquals(E, dir2);

			assertTrue(Files.exists(A));
			assertTrue(Files.exists(B));
			assertTrue(Files.exists(C));
			assertTrue(Files.exists(D));
			assertTrue(Files.exists(E));
		}

		@AfterAll
		static void afterAll() {
			assertTrue(Files.exists(A));
			assertTrue(Files.exists(B));

			assertTrue(Files.notExists(C));
			assertTrue(Files.notExists(D));
			assertTrue(Files.notExists(E));
		}
	}

	static class IllegalUsageTestCase {

		static class MultipleMethodParameters {
			@SuppressWarnings("unused")
			@Test
			void test(@TempDir Path path, @TempDir File file) {
				// never called
			}
		}

		static class InstanceField {

			@TempDir
			Path path;

			@Test
			void test() {
				// never called
			}

		}

		static class MultipleInstanceFields extends InstanceField {

			@TempDir
			File file;
		}

		static class InstanceFieldAndInstanceParameter {

			@TempDir
			Path tempDir;

			@Test
			void test(@SuppressWarnings("unused") @TempDir Path ignored) {
				// never called
			}

		}

		static class StaticField {

			@TempDir
			static Path path;

			@Test
			void test() {
				// never called
			}

		}

		static class MultipleStaticFields extends StaticField {

			@TempDir
			static File file;

		}

		static class StaticAndInstanceField extends StaticField {

			@TempDir
			File file;

		}

		static class StaticFieldAndParameter extends StaticField {

			@BeforeAll
			static void beforeAll(@SuppressWarnings("unused") @TempDir Path ignored) {
				// never called
			}

		}

		static class InstanceFieldAndStaticParameter extends InstanceField {

			@BeforeAll
			static void beforeAll(@SuppressWarnings("unused") @TempDir Path ignored) {
				// never called
			}

		}

	}

}
