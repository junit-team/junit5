/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Bartholdy {

	private static final System.Logger LOG = System.getLogger(Bartholdy.class.getName());

	public static void main(String[] args) {
		System.out.println("Bartholdy " + version());
	}

	public static Path currentJdkHome() {
		var executable = ProcessHandle.current().info().command().map(Path::of).orElseThrow();
		// path element count is 3 or higher: "<JAVA_HOME>/bin/java[.exe]"
		return executable.getParent().getParent().toAbsolutePath();
	}

	/** Return the file name of the uri. */
	static String fileName(URI uri) {
		var urlString = uri.getPath();
		var begin = urlString.lastIndexOf('/') + 1;
		return urlString.substring(begin).split("\\?")[0].split("#")[0];
	}

	public static Path download(URI uri, Path tools) {
		return download(uri, fileName(uri), tools);
	}

	public static Path download(URI uri, String fileName, Path tools) {
		var localPath = tools.resolve(fileName);
		if (Files.exists(localPath)) {
			return localPath;
		}
		try {
			var rbc = Channels.newChannel(uri.toURL().openStream());
			Files.createDirectories(tools);
			try (var fos = new FileOutputStream(localPath.toFile())) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
			return localPath;
		}
		catch (IOException e) {
			throw new UncheckedIOException("download failed", e);
		}
	}

	public static Path install(URI uri, Path tools) {
		return install(uri, fileName(uri), tools);
	}

	public static Path install(URI uri, String zip, Path tools) {
		// uri = "https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.4-bin.zip"
		// zip = "apache-ant-1.10.4-bin.zip"
		var localZip = download(uri, zip, tools);
		try {
			// extract
			var jarTool = ToolProvider.findFirst("jar").orElseThrow();
			var listing = new StringWriter();
			var printWriter = new PrintWriter(listing);
			jarTool.run(printWriter, printWriter, "--list", "--file", localZip.toString());
			// TODO Find better way to extract root folder name...
			var root = Path.of(listing.toString().split("\\R")[0]);
			var home = tools.resolve(root);
			if (Files.notExists(home)) {
				jarTool.run(System.out, System.err, "--extract", "--file", localZip.toString());
				Files.move(root, home);
			}
			// done
			return home.normalize().toAbsolutePath();
		}
		catch (IOException e) {
			throw new UncheckedIOException("install failed", e);
		}
	}

	public static String read(Path jar, String entry, String delimiter, String defaultValue) {
		try (var fs = FileSystems.newFileSystem(jar)) {
			for (var root : fs.getRootDirectories()) {
				var versionPath = root.resolve(entry);
				if (Files.exists(versionPath)) {
					return String.join(delimiter, Files.readAllLines(versionPath));
				}
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("read entry failed", e);
		}
		return defaultValue;
	}

	public static String readProperty(String source, String key, String defaultValue) {
		var properties = new Properties();
		try {
			properties.load(new StringReader(source));
		}
		catch (IOException e) {
			throw new UncheckedIOException("read property failed", e);
		}
		return properties.getProperty(key, defaultValue);
	}

	public static Path setExecutable(Path path) {
		if (Files.isExecutable(path)) {
			return path;
		}
		if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
			LOG.log(System.Logger.Level.DEBUG, "default file system doesn't support posix");
			return path;
		}
		var program = path.toFile();
		var ok = program.setExecutable(true);
		if (!ok) {
			LOG.log(System.Logger.Level.WARNING, "couldn't set executable flag: " + program);
		}
		return path;
	}

	/** Copy source directory to target directory. */
	public static void treeCopy(Path source, Path target) {
		treeCopy(source, target, __ -> true);
	}

	/** Copy source directory to target directory. */
	public static void treeCopy(Path source, Path target, Predicate<Path> filter) {
		LOG.log(System.Logger.Level.DEBUG, "treeCopy(source:`{0}`, target:`{1}`)", source, target);
		if (!Files.exists(source)) {
			return;
		}
		if (!Files.isDirectory(source)) {
			throw new IllegalArgumentException("source must be a directory: " + source);
		}
		if (Files.exists(target)) {
			if (!Files.isDirectory(target)) {
				throw new IllegalArgumentException("target must be a directory: " + target);
			}
			try {
				if (Files.isSameFile(source, target)) {
					return;
				}
			}
			catch (IOException e) {
				throw new UncheckedIOException("copyTree failed", e);
			}
		}
		try (Stream<Path> stream = Files.walk(source).sorted()) {
			int counter = 0;
			List<Path> paths = stream.collect(Collectors.toList());
			for (Path path : paths) {
				Path destination = target.resolve(source.relativize(path));
				if (Files.isDirectory(path)) {
					Files.createDirectories(destination);
					continue;
				}
				if (filter.test(path)) {
					Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
					counter++;
				}
			}
			LOG.log(System.Logger.Level.DEBUG, "copied {0} file(s) of {1} elements...%n", counter, paths.size());
		}
		catch (IOException e) {
			throw new UncheckedIOException("copyTree failed", e);
		}
	}

	/** Delete directory. */
	public static void treeDelete(Path root) {
		treeDelete(root, path -> true);
	}

	/** Delete selected files and directories from the root directory. */
	public static void treeDelete(Path root, Predicate<Path> filter) {
		// simple case: delete existing single file or empty directory right away
		try {
			if (Files.deleteIfExists(root)) {
				return;
			}
		}
		catch (IOException ignored) {
			// fall-through
		}
		// default case: walk the tree...
		try (var stream = Files.walk(root)) {
			var selected = stream.filter(filter).sorted((p, q) -> -p.compareTo(q));
			for (var path : selected.collect(Collectors.toList())) {
				Files.deleteIfExists(path);
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("removing tree failed: " + root, e);
		}
	}

	/** List directory tree structure. */
	public static void treeList(Path root, Consumer<String> out) {
		if (Files.exists(root)) {
			out.accept(root.toString());
		}
		try (Stream<Path> stream = Files.walk(root).sorted()) {
			for (Path path : stream.collect(Collectors.toList())) {
				String string = root.relativize(path).toString();
				String prefix = string.isEmpty() ? "" : File.separator;
				out.accept("." + prefix + string);
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("dumping tree failed: " + root, e);
		}
	}

	public static String version() {
		var loader = Bartholdy.class.getClassLoader();
		try (var is = loader.getResourceAsStream("de/sormuras/bartholdy/version.properties")) {
			if (is == null) {
				return "DEVELOPMENT";
			}
			var properties = new Properties();
			properties.load(is);
			return properties.getProperty("version", "UNKNOWN");
		}
		catch (IOException e) {
			throw new UncheckedIOException("read version failed", e);
		}
	}

	private Bartholdy() {
		throw new UnsupportedOperationException();
	}
}
