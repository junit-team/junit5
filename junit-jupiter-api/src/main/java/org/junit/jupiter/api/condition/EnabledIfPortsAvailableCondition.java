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

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * JUnit Jupiter extension to selectively enable tests when certain ports are available.
 *
 * @see EnabledIfPortsAvailable
 */
class EnabledIfPortsAvailableCondition implements ExecutionCondition {
  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return findAnnotation(context.getElement(), EnabledIfPortsAvailable.class) //
        .map(annotation -> {
          for (int port : annotation.value()) {
            if (!isPortAvailable(port)) {
              return ConditionEvaluationResult.disabled("Port " + port + " is not available.");
            }
          }
          return ConditionEvaluationResult.enabled("All required ports are free");
        }).orElse(ConditionEvaluationResult.enabled(""));
  }

  private static boolean isPortAvailable(int port) {
    try (ServerSocket ss = new ServerSocket()) {
      ss.bind(new InetSocketAddress(port));
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
