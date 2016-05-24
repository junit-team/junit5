/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.gen5.api.extension.AfterEachCallback;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext.Namespace;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.TestExtensionContext;

/**
 * @since 5.0
 */
public class TempDirectory implements AfterEachCallback, MethodParameterResolver {

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Root {
	}

	private static final String KEY = "tempDirectory";

	@Override
	public boolean supports(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {

		return parameter.isAnnotationPresent(Root.class) && parameter.getType() == Path.class;
	}

	@Override
	public Object resolve(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext context) throws ParameterResolutionException {

		return getLocalStore(context).getOrComputeIfAbsent(KEY, key -> createTempDirectory(context));
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		Path tempDirectory = (Path) getLocalStore(context).get(KEY);
		if (tempDirectory != null) {
			delete(tempDirectory);
		}
	}

	private ExtensionContext.Store getLocalStore(ExtensionContext context) {
		return context.getStore(localNamespace(context));
	}

	private Namespace localNamespace(ExtensionContext context) {
		return Namespace.of(TempDirectory.class, context);
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
