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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ResourceSupplier;
import org.junit.platform.commons.util.FileUtils;

public class Temporary implements ResourceSupplier<Path> {

	/**
	 * Same as {@code @New(Temporary.class)}.
	 */
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@New(Temporary.class)
	public @interface Directory {
	}

	private final Path path;

	public Temporary() {
		this("junit-jupiter-temporary");
	}

	public Temporary(String prefix) {
		try {
			this.path = createTemporaryDirectory(prefix);
		}
		catch (IOException e) {
			throw new ParameterResolutionException("creating ", e);
		}
	}

	protected Path createTemporaryDirectory(String prefix) throws IOException {
		return Files.createTempDirectory(prefix + '-');
	}

	protected void deleteTemporaryDirectory() throws IOException {
		FileUtils.deletePath(path);
	}

	@Override
	public Path get() {
		return path;
	}

	@Override
	public Object as(Class<?> parameterType) {
		if (parameterType.isAssignableFrom(File.class)) {
			return path.toFile();
		}
		if (parameterType == URI.class) {
			return path.toUri();
		}
		throw new UnsupportedOperationException("Can't convert Path to " + parameterType);
	}

	@Override
	public void close() {
		try {
			deleteTemporaryDirectory();
		}
		catch (IOException ignore) {
			// for now...
		}
	}
}
