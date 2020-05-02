/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.stream.Collectors.joining;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code TempDirectory} is a JUnit Jupiter extension that creates and cleans
 * up temporary directories if field in a test class or a parameter in a
 * lifecycle method or test method is annotated with {@code @TempDir}.
 *
 * <p>Consult the Javadoc for {@link TempDir} for details on the contract.
 *
 * @since 5.4
 * @see TempDir
 * @see Files#createTempDirectory
 */
class TempDirectory implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

	private static final Namespace NAMESPACE = Namespace.create(TempDirectory.class);
	private static final String KEY = "temp.dir";
	private static final String TEMP_DIR_PREFIX = "junit";

	/**
	 * Perform field injection for non-private, {@code static} fields (i.e.,
	 * class fields) of type {@link Path} or {@link File} that are annotated with
	 * {@link TempDir @TempDir}.
	 */
	@Override
	public void beforeAll(ExtensionContext context) {
		injectStaticFields(context, context.getRequiredTestClass());
	}

	/**
	 * Perform field injection for non-private, non-static fields (i.e.,
	 * instance fields) of type {@link Path} or {@link File} that are annotated
	 * with {@link TempDir @TempDir}.
	 */
	@Override
	public void beforeEach(ExtensionContext context) {
		context.getRequiredTestInstances().getAllInstances() //
				.forEach(instance -> injectInstanceFields(context, instance));
	}

	private void injectStaticFields(ExtensionContext context, Class<?> testClass) {
		injectFields(context, null, testClass, ReflectionUtils::isStatic);
	}

	private void injectInstanceFields(ExtensionContext context, Object instance) {
		injectFields(context, instance, instance.getClass(), ReflectionUtils::isNotStatic);
	}

	private void injectFields(ExtensionContext context, Object testInstance, Class<?> testClass,
			Predicate<Field> predicate) {

		findAnnotatedFields(testClass, TempDir.class, predicate).forEach(field -> {
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
			if (Files.notExists(dir)) {
				return Collections.emptySortedMap();
			}

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
					catch (NoSuchFileException ignore) {
						// ignore
					}
					catch (DirectoryNotEmptyException exception) {
						failures.put(path, exception);
					}
					catch (IOException exception) {
						makeWritableAndTryToDeleteAgain(path, exception);
					}
					return CONTINUE;
				}

				private void makeWritableAndTryToDeleteAgain(Path path, IOException exception) {
					try {
						tryToMakeParentDirsWritable(path);
						makeWritable(path);
						Files.delete(path);
					}
					catch (Exception suppressed) {
						exception.addSuppressed(suppressed);
						failures.put(path, exception);
					}
				}

				private void tryToMakeParentDirsWritable(Path path) {
					Path relativePath = dir.relativize(path);
					Path parent = dir;
					for (int i = 0; i < relativePath.getNameCount(); i++) {
						boolean writable = parent.toFile().setWritable(true);
						if (!writable) {
							break;
						}
						parent = parent.resolve(relativePath.getName(i));
					}
				}

				private void makeWritable(Path path) {
					boolean writable = path.toFile().setWritable(true);
					if (!writable) {
						throw new UndeletableFileException("Attempt to make file '" + path + "' writable failed");
					}
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

	private static class UndeletableFileException extends JUnitException {

		private static final long serialVersionUID = 1L;

		UndeletableFileException(String message) {
			super(message);
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			return this; // Make the output smaller by omitting the stacktrace
		}

	}

}
