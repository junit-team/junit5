/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.io;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code TempDirFactory} defines the SPI for creating temporary directories
 * programmatically.
 *
 * <p>A temporary directory factory is typically used to gain more control on
 * the temporary directory creation, like defining the parent directory or even
 * the file system that should be used.
 *
 * <p>Concrete implementations must have a <em>default constructor</em>.
 *
 * <p>A {@link TempDirFactory} can be configured <em>locally</em>
 * for a test class field or method parameter via the {@link TempDir @TempDir}
 * annotation.
 *
 * @since 5.9
 * @see TempDir @TempDir
 */
@FunctionalInterface
@API(status = EXPERIMENTAL, since = "5.9")
public interface TempDirFactory {

	Path createTempDirectory(String prefix) throws Exception;

	/**
	 * Default temporary directory factory that delegates to
	 * {@link Files#createTempDirectory}.
	 *
	 * @see Files#createTempDirectory(String, FileAttribute[])
	 */
	class Default implements TempDirFactory {

		static final TempDirFactory INSTANCE = new Default();

		public Default() {
		}

		@Override
		public Path createTempDirectory(String prefix) throws IOException {
			return Files.createTempDirectory(prefix);
		}

	}

	/**
	 * Return the {@code TempDirFactory} instance corresponding to the
	 * given {@code Class}.
	 *
	 * @param factoryClass the factory's {@code Class}; never {@code null},
	 * has to be a {@code TempDirFactory} implementation
	 * @return a {@code TempDirFactory} implementation instance
	 */
	static TempDirFactory getTempDirFactory(Class<?> factoryClass) {
		Preconditions.notNull(factoryClass, "Class must not be null");
		Preconditions.condition(TempDirFactory.class.isAssignableFrom(factoryClass),
			"Class must be a TempDirFactory implementation");
		if (factoryClass == TempDirFactory.Default.class) {
			return TempDirFactory.Default.INSTANCE;
		}
		return (TempDirFactory) ReflectionUtils.newInstance(factoryClass);
	}

}
