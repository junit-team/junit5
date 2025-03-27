/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

//tag::user_guide[]
import static java.net.InetAddress.getLoopbackAddress;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

import example.session.CloseableServerSocket;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.store.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

/**
 * Second custom test engine implementation.
 */
public class SecondCustomEngine implements TestEngine {

	public static CloseableServerSocket socket;

	@Override
	public String getId() {
		return "second-custom-test-engine";
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		return new EngineDescriptor(UniqueId.forEngine(getId()), "Second Custom Test Engine");
	}

	@Override
	public void execute(ExecutionRequest request) {
		NamespacedHierarchicalStore<Namespace> store = request.getRequestLevelStore();
		socket = (CloseableServerSocket) store.getOrComputeIfAbsent(Namespace.GLOBAL, "serverSocket", key -> {
			ServerSocket serverSocket;
			try {
				serverSocket = new ServerSocket(0, 50, getLoopbackAddress());
			}
			catch (IOException e) {
				throw new UncheckedIOException("Failed to start ServerSocket", e);
			}
			return new CloseableServerSocket(serverSocket, Executors.newCachedThreadPool());
		});
	}

}
//end::user_guide[]
