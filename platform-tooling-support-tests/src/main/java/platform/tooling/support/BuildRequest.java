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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.3
 */
public class BuildRequest {

	public static Builder builder() {
		return new Builder();
	}

	private String project;
	private String workspace;
	private Path executable;
	private List<Object> arguments = new ArrayList<>();

	public String getProject() {
		return project;
	}

	public String getWorkspace() {
		return workspace;
	}

	public Path getExecutable() {
		return executable;
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public static class Builder {

		private final BuildRequest request = new BuildRequest();

		public BuildRequest build() {
			request.arguments = List.copyOf(request.arguments);
			return request;
		}

		public Builder setProject(String project) {
			request.project = project;
			return this;
		}

		public Builder setExecutable(Path executable) {
			request.executable = executable;
			return this;
		}

		public Builder addArguments(Object... arguments) {
			request.arguments.addAll(List.of(arguments));
			return this;
		}

		public Builder setWorkspace(String workspace) {
			request.workspace = workspace;
			return this;
		}

	}
}
