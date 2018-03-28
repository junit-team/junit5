/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.extensions;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @since 1.0
 */
public class TempDirectory implements AfterEachCallback, ParameterResolver {

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Root {
	}

	private static final String KEY = "tempDirectory";

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(Root.class) && parameterContext.getParameter().getType() == Path.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) {
		return getLocalStore(context).getOrComputeIfAbsent(KEY, key -> createTempDirectory(context));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		Path tempDirectory = (Path) getLocalStore(context).get(KEY);
		if (tempDirectory != null) {
			delete(tempDirectory);
		}
	}

	private ExtensionContext.Store getLocalStore(ExtensionContext context) {
		return context.getStore(localNamespace(context));
	}

	private Namespace localNamespace(ExtensionContext context) {
		return Namespace.create(TempDirectory.class, context);
	}

	private Path createTempDirectory(ExtensionContext context) {
		try {
			String tempDirName;
			if (context.getTestMethod().isPresent()) {
				tempDirName = context.getTestMethod().get().getName();
			}
			else if (context.getTestClass().isPresent()) {
				tempDirName = context.getTestClass().get().getName();
			}
			else {
				tempDirName = context.getDisplayName();
			}

			return Files.createTempDirectory(tempDirName);
		}
		catch (IOException e) {
			throw new ParameterResolutionException("Could not create temp directory", e);
		}
	}

	private void delete(Path tempDirectory) throws IOException {
		Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				return deleteAndContinue(file);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return deleteAndContinue(dir);
			}

			private FileVisitResult deleteAndContinue(Path path) throws IOException {
				Files.delete(path);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
