/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.extensions;

import java.io.IOException;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

// tag::user_guide[]
public class HttpServerExtension implements BeforeAllCallback {
	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		HttpServerResource resource = context.getRoot().getStore(Namespace.GLOBAL).getOrComputeIfAbsent(
			HttpServerResource.class.getName(), key -> {
				try {
					HttpServerResource serverResource = new HttpServerResource(8080);
					serverResource.start();
					return serverResource;
				}
				catch (IOException e) {
					throw new RuntimeException("Failed to create HttpServerResource", e);
				}
			}, HttpServerResource.class);
		// Now you can use the resource within your tests
	}
}
// end::user_guide[]
