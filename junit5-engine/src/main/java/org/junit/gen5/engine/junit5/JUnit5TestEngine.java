package org.junit.gen5.engine.junit5;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanConfiguration;
import org.opentestalliance.AssertionFailedError;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.junit.gen5.commons.util.ReflectionUtils.invokeMethod;
import static org.junit.gen5.commons.util.ReflectionUtils.newInstance;

public class JUnit5TestEngine implements TestEngine {
  // TODO - SBE - could be replace by JUnit5TestEngine.class.getCanonicalName();
  private static final String JUNIT5_ENGINE_ID = "junit5";

  @Override
  public String getId() {
    return JUNIT5_ENGINE_ID;
  }

  @Override
  public List<TestDescriptor> discoverTests(TestPlanConfiguration configuration) {
    List<Class<?>> testClasses = fetchTestClasses(configuration);

    List<TestDescriptor> testDescriptors = testClasses.stream()
        .map(Class::getDeclaredMethods)
        .flatMap(Arrays::stream)
        .filter(method -> method.isAnnotationPresent(Test.class))
        .map(method -> new JavaTestDescriptor(getId(), method))
        .collect(Collectors.toList());

    testDescriptors.addAll(
        configuration.getUniqueIds().stream()
            .map(JavaTestDescriptor::from)
            .collect(Collectors.toList())
    );

    return testDescriptors;
  }

  private List<Class<?>> fetchTestClasses(TestPlanConfiguration testPlanConfiguration) {
    List<Class<?>> testClasses = new LinkedList<>();

    // Add specified test classes directly
    testClasses.addAll(testPlanConfiguration.getClasses());

    // Add test classes by name
    for (String className : testPlanConfiguration.getClassNames()) {
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
        JavaTestDescriptor javaTestDescriptor = (JavaTestDescriptor) testDescriptor;
        Object testInstance = newInstance(javaTestDescriptor.getTestClass());
        invokeMethod(javaTestDescriptor.getTestMethod(), testInstance);
        System.out.println(String.format("Test %s succeeded!", testDescriptor.getUniqueId()));
      } catch (InvocationTargetException e) {
        if (e.getTargetException() instanceof TestSkippedException) {
          System.out.println(String.format("Test %s skipped!", testDescriptor.getUniqueId()));
        } else if (e.getTargetException() instanceof TestAbortedException) {
          System.out.println(String.format("Test %s aborted!", testDescriptor.getUniqueId()));
        } else if (e.getTargetException() instanceof AssertionFailedError) {
          System.out.println(String.format("Test %s failed!", testDescriptor.getUniqueId()));
        } else if (e.getTargetException() instanceof RuntimeException) {
          throw (RuntimeException) e.getTargetException();
        } else {
          throw new RuntimeException(e.getTargetException());
        }
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException(
            String.format("Test %s not well-formed and cannot be executed! ", testDescriptor.getUniqueId()));
      }
    }
  }
}