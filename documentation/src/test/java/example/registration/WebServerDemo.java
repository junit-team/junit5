/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

//tag::user_guide[]
class WebServerDemo {

	// end::user_guide[]
	// @formatter:off
	// tag::user_guide[]
	@RegisterExtension
	static WebServerExtension server = WebServerExtension.builder()
		.enableSecurity(false)
		.build();
	// end::user_guide[]
	// @formatter:on
	// tag::user_guide[]

	@Test
	void getProductList() {
		// end::user_guide[]
		// tag::user_guide[]
		WebClient webClient = new WebClient();
		String serverUrl = server.getServerUrl();
		// Use WebClient to connect to web server using serverUrl and verify response
		assertEquals(200, webClient.get(serverUrl + "/products").getResponseStatus());
	}
}
//end::user_guide[]

class WebServerExtension implements BeforeAllCallback {

	static Builder builder() {
		return new Builder();
	}

	String getServerUrl() {
		return "http://example.org:8181";
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		/* no-op for demo */
	}

	static class Builder {

		public Builder enableSecurity(boolean b) {
			return this;
		}

		public WebServerExtension build() {
			return new WebServerExtension();
		}

	}

}

class WebClient {

	WebResponse get(String string) {
		return new WebResponse();
	}

}

class WebResponse {

	int getResponseStatus() {
		return 200;
	}

}
