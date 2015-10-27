package org.junit.gen5.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestListenerRegistry {
  private static List<TestListener> testListeners = new LinkedList<>();

  public static void registerListener(TestListener testListener) {
    testListeners.add(testListener);
  }

  public static Iterable<TestListener> lookupAllTestListeners() {
    return testListeners;
  }

  public static void notifyListeners(Consumer<TestListener> consumer) {
    lookupAllTestListeners().forEach(consumer);
  }
}