package org.junit.lambda.proposal01;

import static org.junit.Assert.assertEquals;

/**
 * Experiments with before/after methods
 */
public class BeforeAndAfterTest extends JUnitTest {

	Server server;

	{
		beforeAll(() -> {
			server = Server.start();
		});
		
		afterAll(() -> {
			server.shutdown();
		});

		beforeEach(() -> {
			server.prepareResources();
		});

		afterEach(() -> {
			server.clearResources();
		});

		test("Test a", () -> {
			byte[] content = server.get("a");
			assertEquals("foo", new String(content, "UTF-8"));
		});

		test("Test b", () -> {
			byte[] content = server.get("b");
			assertEquals("bar", new String(content, "UTF-8"));
		});

	}
}
