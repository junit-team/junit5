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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.9
 */
class TempDirectoryParameterResolverTests {

	TempDirectory resolver = new TempDirectory();

	@Test
	void supports() {
		Parameter parameter1 = findParameterOfMethod("methodWithTempDirectoryParameter", File.class);
		assertTrue(this.resolver.supportsParameter(parameterContext(parameter1), null));

		Parameter parameter2 = findParameterOfMethod("methodWithoutTempDirectoryParameter", File.class);
		assertFalse(this.resolver.supportsParameter(parameterContext(parameter2), null));
	}

	@Test
	void resolve() {
		Parameter parameter = findParameterOfMethod("methodWithTempDirectoryParameter", File.class);
		assertNotNull(parameter);
		ExtensionContext extensionContext = extensionContext();
		Object file = this.resolver.resolveParameter(parameterContext(parameter), extensionContext);
		assertNotNull(file);
	}

	private Parameter findParameterOfMethod(String methodName, Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(Sample.class, methodName, parameterTypes).get();
		return method.getParameters()[0];
	}

	private static ExtensionContext extensionContext() {

		Path path = mock(Path.class);
		when(path.toFile()).thenReturn(new File(""));

		TempDirectory.CloseablePath closeablePath = mock(TempDirectory.CloseablePath.class);
		when(closeablePath.get()).thenReturn(path);

		ExtensionContext.Store store = mock(ExtensionContext.Store.class);
		when(store.getOrComputeIfAbsent(any(String.class), any(), any())).thenReturn(closeablePath);

		ExtensionContext extensionContext = mock(ExtensionContext.class);
		when(extensionContext.getRoot()).thenReturn(extensionContext);
		when(extensionContext.getStore(any())).thenReturn(store);

		return extensionContext;
	}

	private static ParameterContext parameterContext(Parameter parameter) {
		ParameterContext parameterContext = mock(ParameterContext.class);
		when(parameterContext.getParameter()).thenReturn(parameter);
		when(parameterContext.isAnnotated(TempDir.class)).thenReturn(parameter.getAnnotation(TempDir.class) != null);
		return parameterContext;
	}

	static class Sample {

		void methodWithTempDirectoryParameter(@TempDir File tempDirectory) {

		}

		void methodWithoutTempDirectoryParameter(File normalDirectory) {

		}

	}
}
