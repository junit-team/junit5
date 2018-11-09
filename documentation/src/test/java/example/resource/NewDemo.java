/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example.resource;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ResourceSupplier.New;
import org.junit.jupiter.api.extension.ResourceSupplier.Singleton;

class NewDemo {

	@Test
	void usingFreshServerInstance(@New(WebServer.class) WebServer server) {
		List<String> actual = WebServer.getLines(server.getUri());
		assertLinesMatch(Collections.singletonList("counter = 1"), actual);
	}

	@Test
	void usingSharedServerInstance(@Singleton(WebServer.class) WebServer server) {
		List<String> actual = WebServer.getLines(server.getUri());
		assertLinesMatch(Collections.singletonList("counter = [1|2|3|4]"), actual);
	}

}
