/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.session;

//tag::user_guide[]
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Test;

class HttpTests {

	@Test
	void respondsWith204() throws Exception {
		String port = System.getProperty("http.server.port"); // <1>
		URL url = new URL("http://localhost:" + port + "/test");

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode(); // <2>

		assertEquals(204, responseCode); // <3>
	}
}
//end::user_guide[]
