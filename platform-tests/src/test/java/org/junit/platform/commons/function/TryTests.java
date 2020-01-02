/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;

public class TryTests {

	@Test
	void successfulTriesCanBeTransformed() throws Exception {
		Try<String> t = Try.success("foo");

		assertThat(t.get()).isEqualTo("foo");
		assertThat(t.getOrThrow(RuntimeException::new)).isEqualTo("foo");
		assertThat(t.toOptional()).contains("foo");

		assertThat(t.andThen(v -> {
			assertThat(v).isEqualTo("foo");
			return Try.success("bar");
		}).get()).isEqualTo("bar");
		assertThat(t.andThenTry(v -> {
			assertThat(v).isEqualTo("foo");
			return "bar";
		}).get()).isEqualTo("bar");

		assertThat(t.orElse(() -> fail("should not be called"))).isSameAs(t);
		assertThat(t.orElseTry(() -> fail("should not be called"))).isSameAs(t);

		AtomicReference<String> value = new AtomicReference<>();
		assertThat(t.ifSuccess(value::set)).isSameAs(t);
		assertThat(value.get()).isEqualTo("foo");
		assertThat(t.ifFailure(cause -> fail("should not be called"))).isSameAs(t);
	}

	@Test
	void failedTriesCanBeTransformed() throws Exception {
		JUnitException cause = new JUnitException("foo");
		Try<String> t = Try.failure(cause);

		assertThat(assertThrows(JUnitException.class, t::get)).isSameAs(cause);
		assertThat(assertThrows(RuntimeException.class, () -> t.getOrThrow(RuntimeException::new))).isInstanceOf(
			RuntimeException.class).hasCause(cause);
		assertThat(t.toOptional()).isEmpty();

		assertThat(t.andThen(v -> fail("should not be called"))).isSameAs(t);
		assertThat(t.andThenTry(v -> fail("should not be called"))).isSameAs(t);

		assertThat(t.orElse(() -> Try.success("bar")).get()).isEqualTo("bar");
		assertThat(t.orElseTry(() -> "bar").get()).isEqualTo("bar");

		assertThat(t.ifSuccess(v -> fail("should not be called"))).isSameAs(t);
		AtomicReference<Exception> exception = new AtomicReference<>();
		assertThat(t.ifFailure(exception::set)).isSameAs(t);
		assertThat(exception.get()).isSameAs(cause);
	}

	@Test
	void successfulTriesCanStoreNull() throws Exception {
		Try<String> t = Try.success(null);
		assertThat(t.get()).isNull();
		assertThat(t.getOrThrow(RuntimeException::new)).isNull();
		assertThat(t.toOptional()).isEmpty();
	}

	@Test
	void triesWithSameContentAreEqual() {
		Exception cause = new Exception();
		Callable<Object> failingCallable = () -> {
			throw cause;
		};

		Try<String> success = Try.call(() -> "foo");
		assertThat(success).isEqualTo(success).hasSameHashCodeAs(success);
		assertThat(success).isEqualTo(Try.success("foo"));
		assertThat(success).isNotEqualTo(Try.failure(cause));

		Try<Object> failure = Try.call(failingCallable);
		assertThat(failure).isEqualTo(failure).hasSameHashCodeAs(failure);
		assertThat(failure).isNotEqualTo(Try.success("foo"));
		assertThat(failure).isEqualTo(Try.failure(cause));
	}

	@Test
	void methodPreconditionsAreChecked() {
		assertThrows(JUnitException.class, () -> Try.call(null));

		Try<String> success = Try.success("foo");
		assertThrows(JUnitException.class, () -> success.andThen(null));
		assertThrows(JUnitException.class, () -> success.andThenTry(null));
		assertThrows(JUnitException.class, () -> success.ifSuccess(null));

		Try<String> failure = Try.failure(new Exception());
		assertThrows(JUnitException.class, () -> failure.orElse(null));
		assertThrows(JUnitException.class, () -> failure.orElseTry(null));
		assertThrows(JUnitException.class, () -> failure.ifFailure(null));
	}

}
