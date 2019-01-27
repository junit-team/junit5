/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.support.io;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.stream.Collectors.joining;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code TempDirectory} is a JUnit Jupiter extension that creates and cleans
 * up temporary directories.
 *
 * <p>The temporary directory is only created if a field in a test class or a
 * parameter in a lifecycle method or test method is annotated with
 * {@link TempDir @TempDir}. If the field type or parameter type is neither
 * {@link Path} nor {@link File} or if the temporary directory cannot be created,
 * this extension will throw an {@link ExtensionConfigurationException} or a
 * {@link ParameterResolutionException} as appropriate. In addition, this
 * extension will throw a {@code ParameterResolutionException} for a constructor
 * parameter annotated with {@code @TempDir}.
 *
 * <p>The scope of the temporary directory depends on where the first
 * {@code @TempDir} annotation is encountered when executing a test class. The
 * temporary directory will be shared by all tests in a class when the
 * annotation is present on a {@code static} field or on a parameter of a
 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method. Otherwise &mdash;
 * for example, when {@code @TempDir} is only used on instance fields or on
 * parameters in test, {@link org.junit.jupiter.api.BeforeEach @BeforeEach},
 * or {@link org.junit.jupiter.api.AfterEach @AfterEach} methods &mdash; each test
 * will use its own temporary directory.
 *
 * <p>When the end of the scope of a temporary directory is reached, i.e. when
 * the test method or class has finished execution, this extension will attempt
 * to recursively delete all files and directories in the temporary directory
 * and, finally, the temporary directory itself. In case deletion of a file or
 * directory fails, this extension will throw an {@link IOException} that will
 * cause the test or test class to fail.
 *
 * @since 5.4
 * @see TempDir
 * @see Files#createTempDirectory
 */
@API(status = EXPERIMENTAL, since = "5.4")
public final class TempDirectory implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

	/**
	 * {@code @TempDir} can be used to annotate a field in a test class or a
	 * parameter in a lifecycle method or test method of type {@link Path} or
	 * {@link File} that should be resolved into a temporary directory.
	 *
	 * <p>Please note that {@code @TempDir} is not supported on constructor
	 * parameters. Please use field injection instead, by annotating an
	 * instance field with {@code @TempDir}.
	 *
	 * @see TempDirectory
	 */
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface TempDir {
	}

	private static final Namespace NAMESPACE = Namespace.create(TempDirectory.class);
	private static final String KEY = "temp.dir";
	private static final String TEMP_DIR_PREFIX = "junit";

	/**
	 * Perform field injection for non-private, {@code static} fields (i.e.,
	 * class fields) of type {@link Path} or {@link File} that are annotated with
	 * {@link TempDir @TempDir}.
	 */
	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		injectFields(context, null, ReflectionUtils::isStatic);
	}

	/**
	 * Perform field injection for non-private, non-static fields (i.e.,
	 * instance fields) of type {@link Path} or {@link File} that are annotated
	 * with {@link TempDir @TempDir}.
	 */
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		injectFields(context, context.getRequiredTestInstance(), ReflectionUtils::isNotStatic);
	}

	private void injectFields(ExtensionContext context, Object testInstance, Predicate<Field> predicate) {
		findAnnotatedFields(context.getRequiredTestClass(), TempDir.class, predicate).forEach(field -> {
			assertValidFieldCandidate(field);
			try {
				makeAccessible(field).set(testInstance, getPathOrFile(field.getType(), context));
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	private void assertValidFieldCandidate(Field field) {
		assertSupportedType("field", field.getType());
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException("@TempDir field [" + field + "] must not be private.");
		}
	}

	/**
	 * Determine if the {@link Parameter} in the supplied {@link ParameterContext}
	 * is annotated with {@link TempDir @TempDir}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotated = parameterContext.isAnnotated(TempDir.class);
		if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException(
				"@TempDir is not supported on constructor parameters. Please use field injection instead.");
		}
		return annotated;
	}

	/**
	 * Resolve the current temporary directory for the {@link Parameter} in the
	 * supplied {@link ParameterContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		assertSupportedType("parameter", parameterType);
		return getPathOrFile(parameterType, extensionContext);
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != Path.class && type != File.class) {
			throw new ExtensionConfigurationException("Can only resolve @TempDir " + target + " of type "
					+ Path.class.getName() + " or " + File.class.getName() + " but was: " + type.getName());
		}
	}

	private Object getPathOrFile(Class<?> type, ExtensionContext extensionContext) {
		Path path = extensionContext.getStore(NAMESPACE) //
				.getOrComputeIfAbsent(KEY, key -> createTempDir(), CloseablePath.class) //
				.get();

		return (type == Path.class) ? path : path.toFile();
	}

	private static CloseablePath createTempDir() {
		try {
			return new CloseablePath(Files.createTempDirectory(TEMP_DIR_PREFIX));
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Failed to create default temp directory", ex);
		}
	}

	private static class CloseablePath implements CloseableResource {

		private final Path dir;

		CloseablePath(Path dir) {
			this.dir = dir;
		}

		Path get() {
			return dir;
		}

		@Override
		public void close() throws IOException {
			SortedMap<Path, IOException> failures = deleteAllFilesAndDirectories();
			if (!failures.isEmpty()) {
				throw createIOExceptionWithAttachedFailures(failures);
			}
		}

		private SortedMap<Path, IOException> deleteAllFilesAndDirectories() throws IOException {
			SortedMap<Path, IOException> failures = new TreeMap<>();
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
					return deleteAndContinue(file);
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					return deleteAndContinue(dir);
				}

				private FileVisitResult deleteAndContinue(Path path) {
					try {
						Files.delete(path);
					}
					catch (IOException ex) {
						failures.put(path, ex);
					}
					return CONTINUE;
				}
			});
			return failures;
		}

		private IOException createIOExceptionWithAttachedFailures(SortedMap<Path, IOException> failures) {
			// @formatter:off
			String joinedPaths = failures.keySet().stream()
					.peek(this::tryToDeleteOnExit)
					.map(this::relativizeSafely)
					.map(String::valueOf)
					.collect(joining(", "));
			// @formatter:on
			IOException exception = new IOException("Failed to delete temp directory " + dir.toAbsolutePath()
					+ ". The following paths could not be deleted (see suppressed exceptions for details): "
					+ joinedPaths);
			failures.values().forEach(exception::addSuppressed);
			return exception;
		}

		private void tryToDeleteOnExit(Path path) {
			try {
				path.toFile().deleteOnExit();
			}
			catch (UnsupportedOperationException ignore) {
			}
		}

		private Path relativizeSafely(Path path) {
			try {
				return dir.relativize(path);
			}
			catch (IllegalArgumentException e) {
				return path;
			}
		}
	}

}
