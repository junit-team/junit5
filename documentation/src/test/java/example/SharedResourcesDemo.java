/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static example.Resources.SYSTEM_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.Concurrent;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.Read;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.ReadWrite;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.UseResource;

// tag::user_guide[]
@Execution(Concurrent)
class SharedResourcesDemo {

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
	@UseResource(value = SYSTEM_PROPERTIES, mode = Read)
	void customPropertyIsNotSetByDefault() {
		assertNull(System.getProperty("my.prop"));
	}

	@Test
	@UseResource(value = SYSTEM_PROPERTIES, mode = ReadWrite)
	void canSetCustomPropertyToFoo() {
		System.setProperty("my.prop", "foo");
		assertEquals("foo", System.getProperty("my.prop"));
	}

	@Test
	@UseResource(value = SYSTEM_PROPERTIES, mode = ReadWrite)
	void canSetCustomPropertyToBar() {
		System.setProperty("my.prop", "bar");
		assertEquals("bar", System.getProperty("my.prop"));
	}
}
// end::user_guide[]
