/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import java.io.File;
import java.io.Serializable;
import java.util.Optional;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestSource;

public class FileSystemSource implements TestSource {

	private static final long serialVersionUID = 1L;

	private final File sourceFileOrDirectory;
	private final FilePosition positionInFile;

	public FileSystemSource(File sourceFileOrDirectory) {
		this(sourceFileOrDirectory, null);
	}

	public FileSystemSource(File sourceFileOrDirectory, FilePosition positionInFile) {
		this.sourceFileOrDirectory = Preconditions.notNull(sourceFileOrDirectory,
			"source file or directory must not be null");
		this.positionInFile = positionInFile;
	}

	@Override
	public boolean isJavaClass() {
		return false;
	}

	@Override
	public boolean isJavaMethod() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return !getPosition().isPresent() && sourceFileOrDirectory.isDirectory();
	}

	@Override
	public boolean isFile() {
		return !getPosition().isPresent() && sourceFileOrDirectory.isFile();
	}

	@Override
	public boolean isFilePosition() {
		return getPosition().isPresent();
	}

	static public class FilePosition implements Serializable {

		private static final long serialVersionUID = 1L;

		private final int line;
		private final int column;

		public FilePosition(int line, int column) {
			this.line = line;
			this.column = column;
		}

		public int getLine() {
			return line;
		}

		public int getColumn() {
			return column;
		}

	}

	public File getFile() {
		return sourceFileOrDirectory;
	}

	public Optional<FilePosition> getPosition() {
		return Optional.ofNullable(positionInFile);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(sourceFileOrDirectory.getAbsolutePath());
		getPosition().ifPresent(position -> {
			builder.append(" [");
			builder.append(position.getLine());
			builder.append(':');
			builder.append(position.getColumn());
			builder.append(']');
		});
		return builder.toString();
	}
}
