/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.tool.Maven;

import org.apache.commons.io.FileUtils;

/**
 * @since 1.3
 */
public class Request {

	public static final Path PROJECTS = Paths.get("projects");
	private static final Path TOOLS = Paths.get("build", "test-tools");
	public static final Path WORKSPACE = Paths.get("build", "test-workspace");

	public static Builder builder() {
		return new Builder();
	}

	public static Maven maven() {
		return new Maven(Path.of(System.getProperty("mavenDistribution")));
	}

	private Tool tool;
	private String project;
	private String workspace;
	private List<String> arguments = new ArrayList<>();
	private Map<String, String> environment = new HashMap<>();
	private FileFilter copyProjectToWorkspaceFileFilter;
	private Duration timeout = Duration.ofMinutes(1);

	public String getProject() {
		return project;
	}

	public FileFilter getCopyProjectToWorkspaceFileFilter() {
		return copyProjectToWorkspaceFileFilter;
	}

	public String getWorkspace() {
		return workspace;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public Duration getTimeout() {
		return timeout;
	}

	public Result run() {
		return run(true);
	}

	public Result run(boolean cleanWorkspace) {
		try {
			// sanity check
			if (!Files.isDirectory(PROJECTS)) {
				var cwd = Paths.get(".").normalize().toAbsolutePath();
				throw new IllegalStateException("Directory " + PROJECTS + " not found in: " + cwd);
			}

			Files.createDirectories(TOOLS);
			Files.createDirectories(WORKSPACE);

			var workspace = WORKSPACE.resolve(getWorkspace());
			if (cleanWorkspace) {
				FileUtils.deleteQuietly(workspace.toFile());
				var project = PROJECTS.resolve(getProject());
				if (Files.isDirectory(project)) {
					var filter = getCopyProjectToWorkspaceFileFilter();
					FileUtils.copyDirectory(project.toFile(), workspace.toFile(), filter);
				}
			}

			var configuration = Configuration.builder();
			configuration.setArguments(getArguments());
			configuration.setWorkingDirectory(workspace);
			configuration.setTimeout(getTimeout());
			configuration.getEnvironment().putAll(getEnvironment());

			var result = tool.run(configuration.build());
			System.out.println(result.getOutput("out"));
			System.err.println(result.getOutput("err"));
			return result;
		}
		catch (Exception e) {
			throw new IllegalStateException("run failed", e);
		}
	}

	public static class Builder {

		private final Request request = new Request();

		public Request build() {
			if (request.project == null) {
				throw new IllegalStateException("project must not be null");
			}
			if (request.workspace == null) {
				request.workspace = request.project;
			}
			buildEnvironment(request.environment);
			request.arguments = List.copyOf(request.arguments);
			request.environment = Map.copyOf(request.environment);
			return request;
		}

		private void buildEnvironment(Map<String, String> environment) {
			environment.computeIfAbsent("JUNIT_JUPITER_VERSION", key -> Helper.version("junit-jupiter"));
			environment.computeIfAbsent("JUNIT_VINTAGE_VERSION", key -> Helper.version("junit-vintage"));
			environment.computeIfAbsent("JUNIT_PLATFORM_VERSION", key -> Helper.version("junit-platform"));
		}

		public Builder setTool(Tool tool) {
			request.tool = tool;
			return this;
		}

		public Builder setJavaHome(Path javaHome) {
			return putEnvironment("JAVA_HOME", javaHome.normalize().toAbsolutePath().toString());
		}

		public Builder setProject(String project) {
			request.project = project;
			return this;
		}

		public Builder setProjectToWorkspaceCopyFileFilter(FileFilter copyProjectToWorkspaceFileFilter) {
			request.copyProjectToWorkspaceFileFilter = copyProjectToWorkspaceFileFilter;
			return this;
		}

		public Builder setWorkspace(String workspace) {
			request.workspace = workspace;
			return this;
		}

		public Builder addArguments(Object... arguments) {
			Stream.of(arguments).map(Object::toString).forEach(request.arguments::add);
			return this;
		}

		public Builder putEnvironment(String key, String value) {
			request.environment.put(key, value);
			return this;
		}

		public Builder setTimeout(Duration timeout) {
			request.timeout = timeout;
			return this;
		}
	}
}
