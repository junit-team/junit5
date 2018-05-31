/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.spi.ToolProvider;

import org.apache.commons.io.FileUtils;

/**
 * @since 1.3
 */
class Runner {

	private final Path projects = Paths.get("projects");
	private final Path toolPath = Paths.get("build", "test-tools");
	private final Path workPath = Paths.get("build", "test-workspace");

	private final Request request;
	private final Tool tool;

	Runner(Request request, Tool tool) {
		this.tool = tool;
		this.request = request;
	}

	Result run() throws Exception {
		// sanity check
		if (!Files.isDirectory(projects)) {
			var cwd = Paths.get(".").normalize().toAbsolutePath();
			throw new IllegalStateException("Directory " + projects + " not found in: " + cwd);
		}

		Files.createDirectories(toolPath);
		Files.createDirectories(workPath);

		var result = new Result();
		result.request = request;

		// prepare workspace
		var project = projects.resolve(request.getProject());
		if (!Files.isDirectory(project)) {
			throw new IllegalStateException("Directory " + project + " not found!");
		}
		var workspace = result.workspace = workPath.resolve(request.getWorkspace());
		result.out = workspace.resolve(request.getLogfileOut());
		result.err = workspace.resolve(request.getLogfileErr());

		FileUtils.deleteQuietly(workspace.toFile());
		FileUtils.copyDirectory(project.toFile(), workspace.toFile(), request.getCopyProjectToWorkspaceFileFilter());

		Path executable;
		switch (tool.getKind()) {
			case JDK:
				return runJdkFoundationTool(result);
			case LOCAL:
				executable = tool.computeExecutablePath();
				break;
			case REMOTE:
				executable = installRemoteTool(result).getToolExecutable();
				break;
			default:
				throw new UnsupportedOperationException(tool.getKind() + " is not yet supported");
		}

		var command = new ArrayList<String>();
		command.add(executable.normalize().toAbsolutePath().toString());
		command.addAll(request.getArguments());
		var builder = new ProcessBuilder(command) //
				.directory(workspace.toFile()) //
				.redirectOutput(result.out.toFile()) //
				.redirectError(result.err.toFile());
		if (tool.getKind().equals(Tool.Kind.REMOTE)) {
			builder.environment().put(tool.name() + "_HOME", result.getToolHome().toString());
		}
		builder.environment().putAll(request.getEnvironment());
		var process = builder.start();
		result.status = process.waitFor();
		var encoding = workspace.resolve("file.encoding.txt");
		if (Files.exists(encoding)) {
			result.charset = Charset.forName(new String(Files.readAllBytes(encoding)));
		}
		return result;
	}

	private Result installRemoteTool(Result result) {
		// download
		var version = request.getVersion();
		var toolArchive = tool.computeArchive(version);
		var toolUri = tool.computeUri(version);
		var toolArchivePath = toolPath.resolve(toolArchive);
		if (Files.notExists(toolArchivePath)) {
			var timeout = (int) TimeUnit.MILLISECONDS.convert(9, TimeUnit.SECONDS);
			try {
				FileUtils.copyURLToFile(toolUri.toURL(), toolArchivePath.toFile(), timeout, timeout);
			}
			catch (IOException e) {
				throw new UncheckedIOException("Loading tool failed: " + toolUri, e);
			}
		}

		// extract
		var jarTool = ToolProvider.findFirst("jar").orElseThrow();
		var stringWriter = new StringWriter();
		var printWriter = new PrintWriter(stringWriter);
		jarTool.run(printWriter, printWriter, "--list", "--file", toolArchivePath.toString());
		var toolFolderName = stringWriter.toString().split("\\R")[0];
		var toolFolderPath = toolPath.resolve(toolFolderName);
		if (Files.notExists(toolFolderPath)) {
			try {
				jarTool.run(System.out, System.err, "--extract", "--file", toolArchivePath.toString());
				FileUtils.moveDirectoryToDirectory(Paths.get(toolFolderName).toFile(), toolPath.toFile(), true);
			}
			catch (IOException e) {
				throw new UncheckedIOException("Extracting tool failed: " + toolUri, e);
			}
		}
		result.toolHome = toolFolderPath.normalize().toAbsolutePath();

		// compute program entry point
		var executable = toolFolderPath.resolve(tool.computeExecutablePath());
		if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
			executable.toFile().setExecutable(true);
		}
		result.toolExecutable = executable;
		return result;
	}

	private Result runJdkFoundationTool(Result result) throws Exception {
		var toolProvider = ToolProvider.findFirst(tool.getExecutable()).orElseThrow();
		var args = request.getArguments().toArray(new String[0]);
		try (var out = new PrintStream(Files.newOutputStream(result.out)); //
				var err = new PrintStream(Files.newOutputStream(result.err))) {
			result.status = toolProvider.run(out, err, args);
		}
		return result;
	}
}
