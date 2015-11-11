/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.io.File;
import java.util.Optional;

import lombok.Value;

public class FileSystemSource implements TestSource {

	final private File sourceFileOrDirectory;

	final private FilePosition positionInFile;

	public FileSystemSource(File sourceFileOrDirectory) {
		this(sourceFileOrDirectory, null);
	}

	public FileSystemSource(File sourceFileOrDirectory, FilePosition positionInFile) {
		this.sourceFileOrDirectory = sourceFileOrDirectory;
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

	@Value
	static public class FilePosition {

		final private int line;
		final private int column;
	}

	public Optional<File> getFile() {
		return Optional.ofNullable(sourceFileOrDirectory);
	}

	public Optional<FilePosition> getPosition() {
		return Optional.ofNullable(positionInFile);
	}
}
