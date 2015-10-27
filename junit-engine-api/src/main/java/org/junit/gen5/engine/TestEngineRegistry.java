package org.junit.gen5.engine;

import java.util.ServiceLoader;
import java.util.function.Consumer;

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