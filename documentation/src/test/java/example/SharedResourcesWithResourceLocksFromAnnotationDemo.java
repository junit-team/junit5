/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLocksFrom;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;

// tag::user_guide[]
@Execution(CONCURRENT)
@ResourceLocksFrom(SharedResourcesWithResourceLocksFromAnnotationDemo.Provider.class)
class SharedResourcesWithResourceLocksFromAnnotationDemo {

	private Properties backup;

	@BeforeEach
	void backup() {
		backup = new Properties();
		backup.putAll(System.getProperties());
	}

	@AfterEach
	void restore() {
		System.setProperties(backup);
	}

	@Test
	void customPropertyIsNotSetByDefault() {
		assertNull(System.getProperty("my.prop"));
	}

	@Test
	void canSetCustomPropertyToApple() {
		System.setProperty("my.prop", "apple");
		assertEquals("apple", System.getProperty("my.prop"));
	}

	@Test
	void canSetCustomPropertyToBanana() {
		System.setProperty("my.prop", "banana");
		assertEquals("banana", System.getProperty("my.prop"));
	}

	static final class Provider implements ResourceLocksProvider {

		@Override
		public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
			ResourceAccessMode mode;
			if (testMethod.getName().equals("customPropertyIsNotSetByDefault")) {
				mode = READ;
			}
			else {
				mode = READ_WRITE;
			}
			Set<Lock> locks = new HashSet<>();
			locks.add(new Lock(SYSTEM_PROPERTIES, mode));
			return locks;
		}
	}

}
// end::user_guide[]
