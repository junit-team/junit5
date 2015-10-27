package org.junit.gen5.launcher;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public final class TestPlan {
  private static enum State {
    NEW, ACTIVE, PAUSED, STOPPED, COMPLETED;
  }
  private State state = State.NEW;

  private Collection<TestDescriptor> tests = new LinkedList<>();
  TestPlan() { /* no-op */ }

  public boolean isActive() {
    return this.state == State.ACTIVE;
  }

  public boolean isPaused() {
    return this.state == State.PAUSED;
  }

  public boolean isStopped() {
    return this.state == State.STOPPED;
  }

  public boolean isCompleted() {
    return this.state == State.COMPLETED;
  }

  public void addTest(TestDescriptor testDescriptor) {
    addTests(singleton(testDescriptor));
  }

  public void addTests(TestDescriptor... testDescriptors) {
    addTests(asList(testDescriptors));
  }

  public void addTests(Collection<TestDescriptor> testDescriptors) {
    tests.addAll(testDescriptors);
  }

  public Collection<TestDescriptor> getTests() {
    return Collections.unmodifiableCollection(tests);
  }

  public List<TestDescriptor> getAllTestsForTestEngine(TestEngine testEngine) {
    return tests.stream()
        .filter(testEngine::supports)
        .collect(Collectors.toList());
  }

  public void start() {
    System.out.println("Starting test plan");
    this.state = State.ACTIVE;
  }

  public void stop() {
    System.out.println("Stopping test plan");
    this.state = State.STOPPED;
  }

  public void pause() {
    System.out.println("Pausing test plan");
    this.state = State.PAUSED;
  }

  public void restart() {
    System.out.println("Restarting test plan");
    this.state = State.ACTIVE;
  }
}