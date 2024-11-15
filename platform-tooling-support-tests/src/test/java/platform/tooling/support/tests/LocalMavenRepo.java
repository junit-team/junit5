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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.platform.commons.support.ReflectionSupport.streamFields;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.Preconditions;

@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RUNTIME)
@ExtendWith(LocalMavenRepo.Extension.class)
public @interface LocalMavenRepo {

	class Extension implements ParameterResolver, TestInstancePostProcessor {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return parameterContext.isAnnotated(LocalMavenRepo.class);
		}

		@Override
		public Directory resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			Preconditions.condition(Directory.class.equals(parameterContext.getParameter().getType()),
				() -> "Parameter must be of type " + Directory.class + ": " + parameterContext.getParameter());
			return getOrCreateDirectory(extensionContext);
		}

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
			streamFields(testInstance.getClass(), field -> AnnotationSupport.isAnnotated(field, LocalMavenRepo.class),
				HierarchyTraversalMode.BOTTOM_UP) //
						.forEach(field -> {
							Preconditions.condition(Directory.class.equals(field.getType()),
								() -> "Field must be of type " + Directory.class + ": " + field);
							try {
								field.set(testInstance, getOrCreateDirectory(context));
							}
							catch (IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						});
		}

		private Directory getOrCreateDirectory(ExtensionContext extensionContext) {
			return extensionContext.getRoot().getStore(Namespace.GLOBAL) //
					.getOrComputeIfAbsent(Directory.class, __ -> new Directory(), Directory.class);
		}
	}

	class Directory implements CloseableResource {

		private final Path tempDir;

		private Directory() {
			try {
				tempDir = Files.createTempDirectory("local-maven-repo-");
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public String toCliArgument() {
			return "-Dmaven.repo.local=" + tempDir;
		}

		@Override
		public void close() throws Throwable {
			try (var files = Files.walk(tempDir)) {
				files.sorted(Comparator.<Path> naturalOrder().reversed()) //
						.forEach(path -> {
							try {
								Files.delete(path);
							}
							catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						});
			}
		}
	}
}
