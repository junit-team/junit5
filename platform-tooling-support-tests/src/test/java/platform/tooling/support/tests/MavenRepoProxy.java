/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class MavenRepoProxy implements AutoCloseable {

	// Forbid downloading JUnit artifacts since we want to use the local ones
	private static final List<String> FORBIDDEN_PATHS = List.of("/org/junit");

	private static final List<String> RESTRICTED_HEADER_NAMES = List.of("Connection", "Host");

	private final HttpServer httpServer;
	private final HttpClient httpClient;

	@SuppressWarnings("unused")
	public MavenRepoProxy() throws IOException {
		this(0);
	}

	@SuppressWarnings("unused")
	public MavenRepoProxy(int port) throws IOException {
		this("https://oss.sonatype.org/content/repositories/snapshots", port);
	}

	private MavenRepoProxy(String proxiedUrl, int port) throws IOException {
		httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
		httpServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
		httpServer.createContext("/", exchange -> {
			try (exchange) {
				switch (exchange.getRequestMethod()) {
					case "HEAD":
					case "GET":
						if (FORBIDDEN_PATHS.stream().anyMatch(
							it -> exchange.getRequestURI().getPath().startsWith(it))) {
							exchange.sendResponseHeaders(404, 0);
							break;
						}
						var request = mapRequest(proxiedUrl, exchange);
						try {
							var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
							mapResponse(response, exchange);
						}
						catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						break;
					default:
						exchange.sendResponseHeaders(405, -1);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
		httpServer.start();
	}

	URI getBaseUri() {
		var address = httpServer.getAddress();
		return URI.create("http://" + address.getAddress().getHostName() + ":" + address.getPort());
	}

	private static void mapResponse(HttpResponse<InputStream> response, HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(response.statusCode(),
			response.headers().firstValueAsLong("Content-Length").orElse(0));
		response.headers().map().forEach((key, values) -> exchange.getResponseHeaders().put(key, values));
		try (InputStream body = response.body()) {
			body.transferTo(exchange.getResponseBody());
		}
	}

	private static HttpRequest mapRequest(String proxiedUrl, HttpExchange exchange) {
		var request = HttpRequest.newBuilder().method(exchange.getRequestMethod(), noBody()) //
				.uri(URI.create(proxiedUrl + exchange.getRequestURI().getPath()));
		exchange.getRequestHeaders().entrySet().stream() //
				.filter(entry -> RESTRICTED_HEADER_NAMES.stream().noneMatch(it -> it.equalsIgnoreCase(entry.getKey()))) //
				.forEach(entry -> entry.getValue() //
						.forEach(value -> request.header(entry.getKey(), value)));
		return request.build();
	}

	@Override
	public void close() {
		assertAll( //
			() -> assertDoesNotThrow(() -> httpServer.stop(0)), //
			() -> assertDoesNotThrow(httpClient::close) //
		);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		try (var proxy = new MavenRepoProxy(12345)) {
			System.out.println("Started proxy: " + proxy.getBaseUri());
			while (!Thread.interrupted()) {
				Thread.onSpinWait();
			}
		}
	}
}
