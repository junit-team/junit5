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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.engine.config.JupiterConfiguration.*;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
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
import java.util.Optional;
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
import org.junit.jupiter.api.io.TempDirStrategy;
import org.junit.jupiter.engine.config.EnumConfigurationParameterConverter;
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

		final TempDirStrategy.CleanupMode cleanupMode;
		Optional<TempDirStrategy> optionalTempDirStrategyCleanupStrategy = findAnnotation(testClass, TempDirStrategy.class, true);
		if (optionalTempDirStrategyCleanupStrategy.isPresent()) {
			cleanupMode = optionalTempDirStrategyCleanupStrategy.get().mode();
		} else {
			Optional<TempDirStrategy.CleanupMode> optionalMode = context.getConfigurationParameter(DEFAULT_TEMP_DIR_STRATEGY_CLEANUP_MODE_PROPERTY_NAME, TempDirStrategy.CleanupMode::valueOf);
			cleanupMode = optionalMode.orElse(TempDirStrategy.CleanupMode.ALWAYS);
		}

		findAnnotatedFields(testClass, TempDir.class, predicate).forEach(field -> {
			assertSupportedType("field", field.getType());
			try {
				makeAccessible(field).set(testInstance, getPathOrFile(field, field.getType(), cleanupMode, context));
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});
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
		return getPathOrFile(parameterContext.getParameter(), parameterType, null, extensionContext);
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != Path.class && type != File.class) {
			throw new ExtensionConfigurationException("Can only resolve @TempDir " + target + " of type "
					+ Path.class.getName() + " or " + File.class.getName() + " but was: " + type.getName());
		}
	}

	private Object getPathOrFile(AnnotatedElement sourceElement, Class<?> type, TempDirStrategy.CleanupMode cleanupMode, ExtensionContext extensionContext) {
		Namespace namespace = getScope(extensionContext) == Scope.PER_DECLARATION //
				? NAMESPACE.append(sourceElement) //
				: NAMESPACE;
		Path path = extensionContext.getStore(namespace) //
				.getOrComputeIfAbsent(KEY, __ -> createTempDir(cleanupMode), CloseablePath.class) //
				.get();

		return (type == Path.class) ? path : path.toFile();
	}

	private Scope getScope(ExtensionContext context) {
		return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent( //
			Scope.class, //
			__ -> new EnumConfigurationParameterConverter<>(Scope.class, "@TempDir scope") //
					.get(TEMP_DIR_SCOPE_PROPERTY_NAME, context::getConfigurationParameter, Scope.PER_DECLARATION), //
			Scope.class //
		);
	}

	private static CloseablePath createTempDir(TempDirStrategy.CleanupMode cleanupMode) {
		try {
			return new CloseablePath(Files.createTempDirectory(TEMP_DIR_PREFIX), cleanupMode);
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Failed to create default temp directory", ex);
		}
	}

	private static class CloseablePath implements CloseableResource {

		private final Path dir;
		private final TempDirStrategy.CleanupMode cleanupMode;

		CloseablePath(Path dir, TempDirStrategy.CleanupMode cleanupMode) {
			this.dir = dir;
			this.cleanupMode = cleanupMode;
		}

		Path get() {
			return dir;
		}

		@Override
		public void close() throws IOException {
			if (cleanupMode == TempDirStrategy.CleanupMode.NEVER) {
				return;
			} else if (cleanupMode == TempDirStrategy.CleanupMode.ON_SUCCESS) {
				// todo
			}

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
			resetPermissions(dir);
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (!dir.equals(CloseablePath.this.dir)) {
						resetPermissions(dir);
					}
					return CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					// IOException includes `AccessDeniedException` thrown by non-readable or non-executable flags
					resetPermissionsAndTryToDeleteAgain(file, exc);
					return CONTINUE;
				}

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
						// IOException includes `AccessDeniedException` thrown by non-readable or non-executable flags
						resetPermissionsAndTryToDeleteAgain(path, exception);
					}
					return CONTINUE;
				}

				private void resetPermissionsAndTryToDeleteAgain(Path path, IOException exception) {
					try {
						resetPermissions(path);
						if (Files.isDirectory(path)) {
							Files.walkFileTree(path, this);
						}
						else {
							Files.delete(path);
						}
					}
					catch (Exception suppressed) {
						exception.addSuppressed(suppressed);
						failures.put(path, exception);
					}
				}
			});
			return failures;
		}

		@SuppressWarnings("ResultOfMethodCallIgnored")
		private static void resetPermissions(Path path) {
			File file = path.toFile();
			file.setReadable(true);
			file.setWritable(true);
			if (Files.isDirectory(path)) {
				file.setExecutable(true);
			}
		}

		private IOException createIOExceptionWithAttachedFailures(SortedMap<Path, IOException> failures) {
			// @formatter:off
			String joinedPaths = failures.keySet().stream()
					.map(this::tryToDeleteOnExit)
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

		private Path tryToDeleteOnExit(Path path) {
			try {
				path.toFile().deleteOnExit();
			}
			catch (UnsupportedOperationException ignore) {
			}
			return path;
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

	enum Scope {

		PER_CONTEXT,

		PER_DECLARATION

	}

}
