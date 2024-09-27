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
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import org.apiguardian.api.API;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@code @ResourceLocksProvider} is used to add shared resources
 * to a test class and / or its test methods in runtime.
 *
 * <p>Each shared resource is represented by an instance of {@link Lock}.
 *
 * <p>Adding shared resources using this interface has the same semantics
 * as declaring them via {@code @ResourceLock(value, mode)} annotation
 * but for some cases may be a more flexible and less verbose alternative
 * since it allows to add resources programmatically.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.12
 * @see ResourceLock#providers()
 * @see Resources
 * @see ResourceAccessMode
 * @see Lock
 */
@API(status = EXPERIMENTAL, since = "5.12")
public interface ResourceLocksProvider {

	/**
	 * Add shared resources to a test class.
	 *
	 * <p>Invoked for a test class annotated with
	 * {@code @ResourceLock(providers)} and for its child classes.
	 *
	 * @apiNote Adding {@linkplain Lock a shared resource} with this method
	 * has the same semantics as annotating a test class
	 * with analogous {@code @ResourceLock(value, mode)}.
	 *
	 * @param testClass a test class to add shared resources
	 * @return a set of {@link Lock}; may be empty
	 */
	default Set<Lock> provideForClass(Class<?> testClass) {
		return emptySet();
	}

	/**
	 * Add shared resources to a {@linkplain Nested nested} test class.
	 *
	 * <p>Invoked for a nested test class
	 * annotated with {@code @ResourceLock(providers)}
	 * and for its child classes.
	 *
	 * @apiNote Adding {@linkplain Lock a shared resource} with this method
	 * has the same semantics as annotating a nested test class
	 * with analogous {@code @ResourceLock(value, mode)}.
	 *
	 * @param testClass a nested test class to add shared resources
	 * @return a set of {@link Lock}; may be empty
	 * @see    Nested
	 */
	default Set<Lock> provideForNestedClass(Class<?> testClass) {
		return emptySet();
	}

	/**
	 * Add shared resources to a test method.
	 *
	 * <p>Invoked in case:
	 * <ul>
	 *     <li>an enclosing test class or its parent class
	 *     is annotated with {@code @ResourceLock(providers)}.</li>
	 *     <li>a test method
	 *     is annotated with {@code @ResourceLock(providers)}.</li>
	 * </ul>
	 *
	 * @apiNote Adding {@linkplain Lock a shared resource} with this method
	 * has the same semantics as annotating a test method
	 * with analogous {@code @ResourceLock(value, mode)}.
	 *
	 * @param testClass a test class
	 * or {@linkplain Nested nested} test class containing the {@code testMethod}
	 * @param testMethod a test method to add shared resources
	 * @return a set of {@link Lock}; may be empty
	 */
	default Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
		return emptySet();
	}

	/**
	 *
	 * <p>{@link Lock} represents a shared resource.
	 *
	 * <p>Each resource is identified by {@link #key}.
	 * In addition,{@link #accessMode} allows you to specify
	 * whether a test class or test
	 * method requires {@link ResourceAccessMode#READ_WRITE READ_WRITE}
	 * or only {@link ResourceAccessMode#READ READ} access to the resource.
	 * In the former case, execution of the test will occur while no other test
	 * that uses the shared resource is being executed.
	 * In the latter case, the test class or test method
	 * may be executed concurrently with other tests
	 * that also require {@code READ} access but not at the same time
	 * as any other test that requires {@code READ_WRITE} access.
	 *
	 * <p>This guarantee extends to lifecycle methods of a test class or method.
	 * For example, if a {@code Lock} is added to a test method
	 * then the "lock" will be acquired before any
	 * {@link BeforeEach @BeforeEach} methods are executed and released after all
	 * {@link AfterEach @AfterEach} methods have been executed.
	 *
	 * @apiNote {@link Lock#key} and {@link Lock#accessMode} have the same
	 * semantics as {@link ResourceLock#value()} and {@link ResourceLock#mode()}
	 * respectively.
	 *
	 * @since 5.12
	 * @see Isolated
	 * @see Resources
	 * @see ResourceAccessMode
	 * @see ResourceLock
	 * @see ResourceLocksProvider
	 */
	final class Lock {

		private final String key;

		private final ResourceAccessMode accessMode;

		/**
		 * Create a new {@code Lock} with {@code accessMode = READ_WRITE}.
		 *
		 * @param key the identifier of the resource; never {@code null} or blank
		 * @see ResourceLock#value()
		 */
		public Lock(String key) {
			this(key, ResourceAccessMode.READ_WRITE);
		}

		/**
		 * Create a new {@code Lock}.
		 *
		 * @param key the identifier of the resource; never {@code null} or blank
		 * @param accessMode the lock mode to use to synchronize access to the resource; never {@code null}
		 * @see ResourceLock#value()
		 * @see ResourceLock#mode()
		 */
		public Lock(String key, ResourceAccessMode accessMode) {
			this.key = Preconditions.notBlank(key, "key must not be null or blank");
			this.accessMode = Preconditions.notNull(accessMode, "accessMode must not be null");
		}

		/**
		 * Get the key of this lock.
		 *
		 * @see ResourceLock#value()
		 */
		public String getKey() {
			return key;
		}

		/**
		 * Get the access mode of this lock.
		 *
		 * @see ResourceLock#mode()
		 */
		public ResourceAccessMode getAccessMode() {
			return accessMode;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
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
