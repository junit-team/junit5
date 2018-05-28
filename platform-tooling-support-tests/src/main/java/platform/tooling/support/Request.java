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

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @since 1.3
 */
public class Request {

	public static Builder builder() {
		return new Builder();
	}

	private Tool tool;
	private String version;
	private String project;
	private String workspace;
	private List<String> arguments = new ArrayList<>();
	private Map<String, String> environment = new HashMap<>();
	private String logfileOut = "stdout.txt";
	private String logfileErr = "stderr.txt";
	private FileFilter copyProjectToWorkspaceFileFilter;

	public String getVersion() {
		return version;
	}

	public String getProject() {
		return project;
	}

	public FileFilter getCopyProjectToWorkspaceFileFilter() {
		return copyProjectToWorkspaceFileFilter;
	}

	public String getWorkspace() {
		return workspace;
	}

	public String getLogfileOut() {
		return logfileOut;
	}

	public String getLogfileErr() {
		return logfileErr;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public Result run() {
		var runner = new Runner(this, tool);
		try {
			return runner.run();
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

		public Builder setTool(Tool tool, String version) {
			request.tool = tool;
			request.version = version;
			return this;
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

		public Builder setLogFileNames(String out, String err) {
			request.logfileOut = out;
			request.logfileErr = err;
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
	}
}
