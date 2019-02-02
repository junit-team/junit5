/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example.callbacks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Example of "broken" lifecycle method configuration.
 *
 * <p>Test data is inserted before the database connection has been opened.
 *
 * <p>Database connection is closed before deleting test data.
 */
@ExtendWith({ Extension1.class, Extension2.class })
class BrokenLifecycleMethodConfigDemo {

	@BeforeEach
	void connectToDatabase() {
		System.out.println("  @BeforeEach " + getClass().getSimpleName() + ".connectToDatabase()");
	}

	@BeforeEach
	void insertTestDataIntoDatabase() {
		System.out.println("  @BeforeEach " + getClass().getSimpleName() + ".insertTestDataIntoDatabase()");
	}

	@Test
	void testDatabaseFunctionality() {
		System.out.println("    @Test " + getClass().getSimpleName() + ".testDatabaseFunctionality()");
	}

	@AfterEach
	void deleteTestDataInDatabase() {
		System.out.println("  @AfterEach " + getClass().getSimpleName() + ".deleteTestDataInDatabase()");
	}

	@AfterEach
	void disconnectFromDatabase() {
		System.out.println("  @AfterEach " + getClass().getSimpleName() + ".disconnectFromDatabase()");
	}

}
