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
import static java.net.InetAddress.getLoopbackAddress;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class GlobalSetupTeardownListener implements LauncherSessionListener {

	private Fixture fixture;

	@Override
	public void launcherSessionOpened(LauncherSession session) {
		// Avoid setup for test discovery by delaying it until tests are about to be executed
		session.getLauncher().registerTestExecutionListeners(new TestExecutionListener() {
			@Override
			public void testPlanExecutionStarted(TestPlan testPlan) {
				if (fixture == null) {
					fixture = new Fixture();
					fixture.setUp();
				}
			}
		});
	}

	@Override
	public void launcherSessionClosed(LauncherSession session) {
		if (fixture != null) {
			fixture.tearDown();
			fixture = null;
		}
	}

	static class Fixture {

		private HttpServer server;
		private ExecutorService executorService;

		void setUp() {
			//end::user_guide[]
			System.out.println("Starting HTTP server...");
			//tag::user_guide[]
			try {
				server = HttpServer.create(new InetSocketAddress(getLoopbackAddress(), 0), 0);
			}
			catch (IOException e) {
				throw new UncheckedIOException("Failed to start HTTP server", e);
			}
			server.createContext("/test", exchange -> {
				exchange.sendResponseHeaders(204, -1);
				exchange.close();
			});
			executorService = Executors.newCachedThreadPool();
			server.setExecutor(executorService);
			server.start(); // <1>
			int port = server.getAddress().getPort();
			System.setProperty("http.server.port", String.valueOf(port)); // <2>
			//end::user_guide[]
			System.out.println("Started HTTP server with address " + server.getAddress());
			//tag::user_guide[]
		}

		void tearDown() {
			//end::user_guide[]
			System.out.println("Stopping HTTP server with address " + server.getAddress());
			//tag::user_guide[]
			server.stop(0); // <3>
			executorService.shutdownNow();
		}
	}

}
//end::user_guide[]
