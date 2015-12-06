package org.junit.gen5.engine.junit5ext.samples;

import org.junit.gen5.api.Test;

public class SinglePassingTestSampleClass {
  @Test
  void singlePassingTest() throws Exception {
    System.out.println("Test got executed!");
  }
}
