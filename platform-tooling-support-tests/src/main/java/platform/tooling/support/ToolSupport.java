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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.spi.ToolProvider;

import org.apache.commons.io.FileUtils;

/**
 * @since 1.3
 */
public class ToolSupport {

	private static final String WRAPPER = "<wrapper>";

	private final BuildTool tool;
	private final String version;

	private final Path toolPath = Paths.get("build", "test-tools");
	private final Path workPath = Paths.get("build", "test-workspace");

	public ToolSupport(BuildTool tool) {
		this(tool, WRAPPER);
	}

	public ToolSupport(BuildTool tool, String version) {
		this.tool = tool;
		this.version = version;
	}

	/** Get build tool. */
	public Path init() throws Exception {
		// sanity check
		var projects = Paths.get("projects");
		if (!Files.isDirectory(projects)) {
			var cwd = Paths.get(".").normalize().toAbsolutePath();
			throw new IllegalStateException("Directory " + projects + " not found in: " + cwd);
		}

		Files.createDirectories(toolPath);
		Files.createDirectories(workPath);

		var windows = System.getProperty("os.name").toLowerCase().contains("win");

		// trivial case: use "gradlew" from "junit5" main project
		if (BuildTool.GRADLE.equals(tool) && WRAPPER.equals(version)) {
			var executable = "gradlew";
			if (windows) {
				executable += ".bat";
			}
			return Paths.get("..", executable).normalize().toAbsolutePath();
		}

		// download
		var toolArchive = tool.createArchive(version);
		var toolUri = tool.createUri(version);
		var toolArchivePath = toolPath.resolve(toolArchive);
		if (Files.notExists(toolArchivePath)) {
			FileUtils.copyURLToFile(toolUri.toURL(), toolArchivePath.toFile(), 5000, 5000);
		}

		// extract
		var jarTool = ToolProvider.findFirst("jar").orElseThrow();
		var stringWriter = new StringWriter();
		var printWriter = new PrintWriter(stringWriter);
		jarTool.run(printWriter, printWriter, "--list", "--file", toolArchivePath.toString());
		var toolFolderName = stringWriter.toString().split("\\R")[0];
		var toolFolderPath = toolPath.resolve(toolFolderName);
		if (Files.notExists(toolFolderPath)) {
			jarTool.run(System.out, System.err, "--extract", "--file", toolArchivePath.toString());
			FileUtils.moveDirectoryToDirectory(Paths.get(toolFolderName).toFile(), toolPath.toFile(), true);
		}
		var executable = toolFolderPath.resolve(tool.createExecutable()).toAbsolutePath();
		if (!windows) {
			executable.toFile().setExecutable(true);
		}
		return executable;
	}

	public BuildResult run(BuildRequest request) throws Exception {
		var result = new BuildResult();

		// unroll to clean "build/test-workspace/${name}" workspace directory
		var project = Paths.get("projects", request.getProject());
		var workspace = result.workspace = workPath.resolve(request.getWorkspace());
		FileUtils.deleteQuietly(workspace.toFile());
		FileUtils.copyDirectory(project.toFile(), workspace.toFile());

		// execute build and collect data
		result.out = workspace.resolve("stdout.txt");
		result.err = workspace.resolve("stderr.txt");

		var command = new ArrayList<String>();
		command.add(request.getExecutable().toString());
		request.getArguments().forEach(arg -> command.add(arg.toString()));
		var builder = new ProcessBuilder(command) //
				.directory(workspace.toFile()) //
				.redirectOutput(result.out.toFile()) //
				.redirectError(result.err.toFile());
		var process = builder.start();
		result.status = process.waitFor();
		var encoding = workspace.resolve("file.encoding.txt");
		if (Files.exists(encoding)) {
			result.charset = Charset.forName(new String(Files.readAllBytes(encoding)));
		}
		return result;
	}
}
