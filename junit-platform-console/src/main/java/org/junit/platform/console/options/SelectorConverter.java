/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectIteration;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectModule;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.IterationSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;

import picocli.CommandLine.ITypeConverter;

class SelectorConverter {

	static class Module implements ITypeConverter<ModuleSelector> {
		@Override
		public ModuleSelector convert(String value) {
			return selectModule(value);
		}
	}

	static class Uri implements ITypeConverter<UriSelector> {
		@Override
		public UriSelector convert(String value) {
			return selectUri(value);
		}
	}

	static class File implements ITypeConverter<FileSelector> {
		@Override
		public FileSelector convert(String value) {
			return selectFile(value);
		}
	}

	static class Directory implements ITypeConverter<DirectorySelector> {
		@Override
		public DirectorySelector convert(String value) {
			return selectDirectory(value);
		}
	}

	static class Package implements ITypeConverter<PackageSelector> {
		@Override
		public PackageSelector convert(String value) {
			return selectPackage(value);
		}
	}

	static class Class implements ITypeConverter<ClassSelector> {
		@Override
		public ClassSelector convert(String value) {
			return selectClass(value);
		}
	}

	static class Method implements ITypeConverter<MethodSelector> {
		@Override
		public MethodSelector convert(String value) {
			return selectMethod(value);
		}
	}

	static class ClasspathResource implements ITypeConverter<ClasspathResourceSelector> {
		@Override
		public ClasspathResourceSelector convert(String value) {
			return selectClasspathResource(value);
		}
	}

	static class Iteration implements ITypeConverter<IterationSelector> {

		public static final Pattern PATTERN = Pattern.compile(
			"(?<type>[a-z]+):(?<value>.*)\\[(?<indices>(\\d+)(\\.\\.\\d+)?(\\s*,\\s*(\\d+)(\\.\\.\\d+)?)*)]");

		@Override
		public IterationSelector convert(String value) {
			Matcher matcher = PATTERN.matcher(value);
			Preconditions.condition(matcher.matches(), "Invalid format: must be TYPE:VALUE[INDEX(,INDEX)*]");
			DiscoverySelector parentSelector = createParentSelector(matcher.group("type"), matcher.group("value"));
			int[] iterationIndices = Arrays.stream(matcher.group("indices").split(",")) //
					.flatMapToInt(this::parseIndexDefinition) //
					.toArray();
			return selectIteration(parentSelector, iterationIndices);
		}

		private IntStream parseIndexDefinition(String value) {
			String[] parts = value.split("\\.\\.", 2);
			int firstIndex = Integer.parseInt(parts[0]);
			if (parts.length == 2) {
				int lastIndex = Integer.parseInt(parts[1]);
				return IntStream.rangeClosed(firstIndex, lastIndex);
			}
			return IntStream.of(firstIndex);
		}

		private DiscoverySelector createParentSelector(String type, String value) {
			switch (type) {
				case "module":
					return selectModule(value);
				case "uri":
					return selectUri(value);
				case "file":
					return selectFile(value);
				case "directory":
					return selectDirectory(value);
				case "package":
					return selectPackage(value);
				case "class":
					return selectClass(value);
				case "method":
					return selectMethod(value);
				case "resource":
					return selectClasspathResource(value);
				default:
					throw new IllegalArgumentException("Unknown type: " + type);
			}
		}
	}

}
