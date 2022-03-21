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

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Result of a tool run. */
public interface Result {

	static Builder builder() {
		return new Builder();
	}

	int getExitCode();

	Duration getDuration();

	default String getOutput(String key) {
		return String.join("\n", getOutputLines(key));
	}

	List<String> getOutputLines(String key);

	boolean isTimedOut();

	class Builder implements Result {

		private int exitCode = Integer.MIN_VALUE;
		private Duration duration = Duration.ZERO;
		private Map<String, List<String>> lines = new HashMap<>();
		private boolean timedOut;

		public Result build() {
			requireNonNull(duration, "duration must not be null");
			return this;
		}

		@Override
		public String toString() {
			return "Result{" + "exitCode=" + exitCode + ", timedOut=" + timedOut + ", duration=" + duration + ", lines="
					+ lines + '}';
		}

		@Override
		public int getExitCode() {
			return exitCode;
		}

		public Builder setExitCode(int exitCode) {
			this.exitCode = exitCode;
			return this;
		}

		@Override
		public Duration getDuration() {
			return duration;
		}

		public Builder setDuration(Duration duration) {
			this.duration = duration;
			return this;
		}

		public Builder setOutput(String key, String output) {
			return setOutput(key, List.of(output.split("\\R")));
		}

		public Builder setOutput(String key, List<String> output) {
			this.lines.put(key, output);
			return this;
		}

		@Override
		public List<String> getOutputLines(String key) {
			return lines.getOrDefault(key, List.of());
		}

		@Override
		public boolean isTimedOut() {
			return timedOut;
		}

		public Builder setTimedOut(boolean timedOut) {
			this.timedOut = timedOut;
			return this;
		}
	}
}
