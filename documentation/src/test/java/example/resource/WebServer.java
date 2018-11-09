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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.extension.ResourceSupplier;

public class WebServer implements ResourceSupplier<HttpServer>, HttpHandler {

	public static List<String> getLines(URI uri) {
		String result;
		try {
			try (InputStream inputStream = uri.toURL().openStream()) {
				result = new Scanner(inputStream).useDelimiter("\\A").next();
			}
		}
		catch (IOException exception) {
			result = exception.toString();
		}
		return Arrays.asList(result.split("\\R"));
	}

	private final AtomicInteger counter;
	private final HttpServer httpServer;

	public WebServer() {
		this.counter = new AtomicInteger();
		this.httpServer = createAndStartHttpServerOnFreePort();
	}

	private HttpServer createAndStartHttpServerOnFreePort() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.createContext("/", this);
			server.start();
			return server;
		}
		catch (IOException e) {
			throw new UncheckedIOException("Creating HttpServer failed!", e);
		}
	}

	@Override
	public void close() {
		httpServer.stop(1);
	}

	@Override
	public HttpServer get() {
		return httpServer;
	}

	public URI getUri() {
		String host = httpServer.getAddress().getHostName();
		int port = httpServer.getAddress().getPort();
		return URI.create("http://" + host + ":" + port);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String response = "counter = " + counter.incrementAndGet();
		exchange.sendResponseHeaders(200, response.length());
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(response.getBytes());
		}
	}
}
