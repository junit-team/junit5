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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
// tag::user_guide_example[]
class AutoCloseDemo {

	@AutoClose
	Connection connection = getJdbcConnection("jdbc:mysql://localhost/testdb");

	@Test
	void usersTableHasEntries() throws SQLException {
		ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM users");

		assertTrue(resultSet.next());
	}

	// ...
	// end::user_guide_example[]
	private static Connection getJdbcConnection(String url) {
		try {
			return DriverManager.getConnection(url);
		}
		catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

}
