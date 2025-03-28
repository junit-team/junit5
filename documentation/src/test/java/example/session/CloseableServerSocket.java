/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.session;

//tag::user_guide[]
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

public class CloseableServerSocket implements AutoCloseable {

	private final ServerSocket serverSocket;
	private final ExecutorService executorService;

	public CloseableServerSocket(ServerSocket serverSocket, ExecutorService executorService) {
		this.serverSocket = serverSocket;
		this.executorService = executorService;
	}

	@Override
	public void close() {
		try {
			serverSocket.close();
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to close ServerSocket", e);
		}
		executorService.shutdownNow();
	}
}
//end::user_guide[]
