package org.junit.gen5.launcher;

import org.junit.gen5.engine.TestEngine;

import java.util.ServiceLoader;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestEngineRegistry {
  private static Iterable<TestEngine> testEngines;

  public static Iterable<TestEngine> lookupAllTestEngines() {
    if (testEngines == null) {
      testEngines = ServiceLoader.load(TestEngine.class);
    }
    return testEngines;
  }
}