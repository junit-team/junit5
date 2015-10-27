package org.junit.gen5.engine;

import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestListenerRegistry {
  private static Iterable<TestListener> testListeners;

  public static Iterable<TestListener> lookupAllTestListeners() {
    if (testListeners == null) {
      testListeners = ServiceLoader.load(TestListener.class);
    }
    return testListeners;
  }

  public static void notifyListeners(Consumer<TestListener> consumer) {
    lookupAllTestListeners().forEach(consumer);
  }
}