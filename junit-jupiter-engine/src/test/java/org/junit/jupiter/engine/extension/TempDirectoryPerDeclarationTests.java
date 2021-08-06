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

import static org.assertj.core.api.Assertions.assertThat;
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

import org.assertj.core.api.Condition;
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
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * Integration tests for the new behavior of the {@link TempDirectory} extension
 * to create separate temp directories for each {@link TempDir} declaration.
 *
 * @since 5.8
 */
@DisplayName("TempDirectory extension (per declaration)")
class TempDirectoryPerDeclarationTests extends AbstractJupiterTestEngineTests {

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
	@DisplayName("is capable of removal of non-executable, non-writable, or non-readable directories and folders")
	void nonMintPermissionsContentDoesNotCauseFailure() {
		executeTestsForClass(NonMintPermissionContentInTempDirectoryDoesNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(13).succeeded(13));
	}

	@Test
	@DisplayName("is capable of removal when its permissions were been changed")
	void nonMintPermissionsDoNotCauseFailure() {
		executeTestsForClass(NonMintTempDirectoryPermissionsDoNotCauseFailureTestCase.class).testEvents()//
				.assertStatistics(stats -> stats.started(42).succeeded(42));
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
			TempDirectoryPerDeclarationTests.assertSingleFailedTest(results,
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

	// -------------------------------------------------------------------------

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

	static class InvalidTestCase {

		@Test
		void wrongParameterType(@SuppressWarnings("unused") @TempDir String ignored) {
			fail("this should never be called");
		}
	}

	@Nested
	@DisplayName("resolves java.io.File injection type")
	class FileAndPathInjection {

		@TempDir
		File fileTempDir;

		@TempDir
		Path pathTempDir;

		@Test
		@DisplayName("and injected File and Path reference the same temp directory")
		void checkFile(@TempDir File tempDir, @TempDir Path ref) {
			assertNotNull(tempDir);
			assertNotNull(ref);
			assertNotNull(this.fileTempDir);
			assertNotNull(this.pathTempDir);
		}

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
