/*
 * Copyright 2015-2024 the original author or authors.
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
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Test;

class HttpTests {

	@Test
	void respondsWith204() throws Exception {
		String host = System.getProperty("http.server.host"); // <1>
		String port = System.getProperty("http.server.port"); // <2>
		URL url = URI.create("http://" + host + ":" + port + "/test").toURL();

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode(); // <3>

		assertEquals(204, responseCode); // <4>
	}
}
//end::user_guide[]
