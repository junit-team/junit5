package org.junit.gen5.engine.junit5;

import static java.lang.String.format;
import static org.junit.gen5.commons.util.ReflectionUtils.invokeMethod;
import static org.junit.gen5.commons.util.ReflectionUtils.newInstance;
import static org.junit.gen5.engine.TestListenerRegistry.notifyTestExecutionListeners;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class JUnit5TestEngine implements TestEngine {
  // TODO - SBE - could be replace by JUnit5TestEngine.class.getCanonicalName();
  private static final String JUNIT5_ENGINE_ID = "junit5";

  @Override
  public String getId() {
    return JUNIT5_ENGINE_ID;
  }

  @Override
  public List<TestDescriptor> discoverTests(TestPlanSpecification specification) {
    List<Class<?>> testClasses = fetchTestClasses(specification);

    List<TestDescriptor> testDescriptors = testClasses.stream()
        .map(Class::getDeclaredMethods)
        .flatMap(Arrays::stream)
        .filter(method -> method.isAnnotationPresent(Test.class))
        .map(method -> new JavaTestDescriptor(getId(), method))
        .peek(testDescriptor -> notifyTestExecutionListeners(testListener -> testListener.testFound(testDescriptor)))
        .collect(Collectors.toList());

    testDescriptors.addAll(
        specification.getUniqueIds().stream()
            .map(JavaTestDescriptor::from)
            .peek(testDescriptor -> notifyTestExecutionListeners(testListener -> testListener.testFound(testDescriptor)))
            .collect(Collectors.toList())
    );

    return testDescriptors;
  }

  private List<Class<?>> fetchTestClasses(TestPlanSpecification testPlanSpecification) {
    List<Class<?>> testClasses = new LinkedList<>();

    // Add specified test classes directly
    testClasses.addAll(testPlanSpecification.getClasses());

    // Add test classes by name
    for (String className : testPlanSpecification.getClassNames()) {
      try {
        testClasses.add(Class.forName(className));
      } catch (ClassNotFoundException e) {
        String msg = "Could not find test class '%s' in the classpath!";
        throw new IllegalArgumentException(format(msg, className));
      }
    }

    // TODO - SBE - Add classes for packages
    // TODO - SBE - Add classes for package names
    // TODO - SBE - Add classes for paths
    // TODO - SBE - Add classes for file names

    return testClasses;
  }

  @Override
  public boolean supports(TestDescriptor testDescriptor) {
    return testDescriptor instanceof JavaTestDescriptor;
  }

  @Override
  public void execute(Collection<TestDescriptor> testDescriptors) {
    for (TestDescriptor testDescriptor : testDescriptors) {
      try {
        notifyTestExecutionListeners(testListener -> testListener.testStarted(testDescriptor));
        JavaTestDescriptor javaTestDescriptor = (JavaTestDescriptor) testDescriptor;
        Object testInstance = newInstance(javaTestDescriptor.getTestClass());
        invokeMethod(javaTestDescriptor.getTestMethod(), testInstance);
        notifyTestExecutionListeners(testListener -> testListener.testSucceeded(testDescriptor));
      } catch (InvocationTargetException e) {
        if (e.getTargetException() instanceof TestSkippedException) {
          notifyTestExecutionListeners(testListener -> testListener.testSkipped(testDescriptor, e.getTargetException()));
        } else if (e.getTargetException() instanceof TestAbortedException) {
          notifyTestExecutionListeners(testListener -> testListener.testAborted(testDescriptor, e.getTargetException()));
        } else {
          notifyTestExecutionListeners(testListener -> testListener.testFailed(testDescriptor, e.getTargetException()));
        }
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException(
            String.format("Test %s not well-formed and cannot be executed! ", testDescriptor.getUniqueId()));
      }
    }
  }
}