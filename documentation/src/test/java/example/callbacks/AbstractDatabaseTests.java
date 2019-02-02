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

import static example.callbacks.Logger.afterEachMethod;
import static example.callbacks.Logger.beforeEachMethod;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract base class for tests that use the database.
 */
abstract class AbstractDatabaseTests {

	@BeforeEach
	void connectToDatabase() {
		beforeEachMethod(AbstractDatabaseTests.class.getSimpleName() + ".connectToDatabase()");
	}

	@AfterEach
	void disconnectFromDatabase() {
		afterEachMethod(AbstractDatabaseTests.class.getSimpleName() + ".disconnectFromDatabase()");
	}

}
