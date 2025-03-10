/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import java.util.Objects;
import java.util.Optional;

final class DefaultEngineDiscoveryIssue implements EngineDiscoveryIssue {

	private final Severity severity;
	private final String message;
	private final DiscoverySelector selector;
	private final TestSource source;
	private final Throwable cause;

	DefaultEngineDiscoveryIssue(Builder builder) {
		this.severity = builder.severity;
		this.message = builder.message;
		this.selector = builder.selector;
		this.source = builder.source;
		this.cause = builder.cause;
	}

	@Override
	public Severity severity() {
		return this.severity;
	}

	@Override
	public String message() {
		return this.message;
	}

	@Override
	public Optional<DiscoverySelector> selector() {
		return Optional.ofNullable(this.selector);
	}

	@Override
	public Optional<TestSource> source() {
		return Optional.ofNullable(this.source);
	}

	@Override
	public Optional<Throwable> cause() {
		return Optional.ofNullable(this.cause);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		DefaultEngineDiscoveryIssue that = (DefaultEngineDiscoveryIssue) o;
		return this.severity == that.severity //
				&& Objects.equals(this.message, that.message) //
				&& Objects.equals(this.selector, that.selector) //
				&& Objects.equals(this.source, that.source);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.severity, this.message, this.selector, this.source);
	}

	static class Builder implements EngineDiscoveryIssue.Builder {

		private final Severity severity;
		private final String message;
		private DiscoverySelector selector;
		private TestSource source;
		public Throwable cause;

		Builder(Severity severity, String message) {
			this.severity = severity;
			this.message = message;
		}

		@Override
		public Builder selector(DiscoverySelector selector) {
			this.selector = selector;
			return this;
		}

		@Override
		public Builder source(TestSource source) {
			this.source = source;
			return this;
		}

		@Override
		public Builder cause(Throwable cause) {
			this.cause = cause;
			return this;
		}

		@Override
		public EngineDiscoveryIssue build() {
			return new DefaultEngineDiscoveryIssue(this);
		}
	}
}
