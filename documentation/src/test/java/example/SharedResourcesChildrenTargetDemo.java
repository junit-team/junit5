/*
 * Copyright 2015-2023 the original author or authors.
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
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_OUT;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;
import static org.junit.jupiter.api.parallel.Resources.TIME_ZONE;

import java.util.Properties;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLockTarget;

// tag::user_guide[]
@Execution(CONCURRENT)
@ResourceLock(value = TIME_ZONE, mode = READ, target = ResourceLockTarget.CHILDREN)
class SharedResourcesChildrenTargetDemo {

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
	@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ)
	void usePropertiesAndTimeZoneWithoutModification() {
		assertNull(System.getProperty("my.prop"));
	}

	@Test
	@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ)
	void usePropertiesAndTimeZoneWithoutModificationAgain() {
		assertNull(System.getProperty("my.prop"));
	}

	@Test
	@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
	void canSetCustomPropertyToTimeZone() {
		String timezone = TimeZone.getDefault().getDisplayName();
		System.setProperty("my.timezone", timezone);
		assertEquals(timezone, System.getProperty("my.timezone"));
	}

}
// end::user_guide[]
