/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * Unit tests for {@link EnabledIfPortsAvailable}.
 */
public class EnabledIfPortsAvailableConditionTests {

  private static ServerSocket server;

  @BeforeAll
  static void setupSocket2000() {
    try {
      // Best effort to force some tests to be skipped.
      server = new ServerSocket(2000);
    } catch (IOException exception) {
      // It is fine if we don't manage to bind the socket
      // tests should not fail cause of it.
    }
  }

  @AfterAll
  static void tearDownSocket2000() throws IOException {
    if (server != null) {
      server.close();
      server = null;
    }
  }

  @Test
  @EnabledIfPortsAvailable(2000)
  void testPortMostLikelyInUse() throws IOException {
    // Most of the time the test should be skipped since we are explicitly binding the port with server.
    // If we fail to bind the server to the given port:
    // - due to the port that is already taken; in that case the test should be skipped via the annotation
    // - due to another reason (port is free); in that case the test should pass
    try (ServerSocket s = new ServerSocket()) {
      s.bind(new InetSocketAddress(2000));
    }
  }

  @Test
  @EnabledIfPortsAvailable(2001)
  void testPortMostLikelyAvailableV1() throws IOException {
    // Most of the time the test should pass. In some cases it may happen that the port 2001
    // is bound by somebody else so the test should be skipped.
    try (ServerSocket s = new ServerSocket()) {
      s.bind(new InetSocketAddress(2001));
    }
  }

  @Test
  @EnabledIfPortsAvailable(5050)
  void testPortMostLikelyAvailableV2() throws IOException {
    // Most of the time the test should pass. In some cases it may happen that the port 5050
    // is bound by somebody else so the test should be skipped.
    try (ServerSocket s = new ServerSocket()) {
      s.bind(new InetSocketAddress(5050));
    }
  }

}
