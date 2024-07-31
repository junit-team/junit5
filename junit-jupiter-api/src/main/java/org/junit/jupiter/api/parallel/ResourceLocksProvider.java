/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static java.util.Collections.emptySet;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

@API(status = STABLE, since = "5.10")
public interface ResourceLocksProvider {
	default Set<Lock> provideForClass(Class<?> testClass) {
		return emptySet();
	}

	default Set<Lock> provideForNestedClass(Class<?> testClass) {
		return emptySet();
	}

	default Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
		return emptySet();
	}

	final class Lock {

		private final String key;

		private final ResourceAccessMode accessMode;

		public Lock(String key) {
			this(key, ResourceAccessMode.READ_WRITE);
		}

		public Lock(String key, ResourceAccessMode accessMode) {
			this.key = Preconditions.notBlank(key, "key must not be blank");
			this.accessMode = Preconditions.notNull(accessMode, "accessMode must not be null");
		}

		public String getKey() {
			return key;
		}

		public ResourceAccessMode getAccessMode() {
			return accessMode;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Lock lock = (Lock) o;
			return Objects.equals(key, lock.key) && accessMode == lock.accessMode;
		}

		@Override
		public int hashCode() {
			return Objects.hash(key, accessMode);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this) //
					.append("key", key) //
					.append("accessMode", accessMode) //
					.toString();
		}
	}

}
