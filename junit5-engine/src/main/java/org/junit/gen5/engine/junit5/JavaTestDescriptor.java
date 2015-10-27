
package org.junit.gen5.engine.junit5;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.junit.gen5.commons.util.ObjectUtils.nullSafeToString;

/**
 * {@link TestDescriptor} for tests based on Java classes and methods.
 *
 * <p>The pattern of the {@link #getTestId test ID} takes the form of
 * <code>{fully qualified class name}#{method name}({comma separated list
 * of method parameter types})</code>, where each method parameter type is
 * a fully qualified class name or a primitive type. For example,
 * {@code org.example.MyTests#test()} references the {@code test()} method
 * in the {@code org.example.MyTests} class that does not accept parameters.
 * Similarly, {@code org.example.MyTests#test(java.lang.String, java.math.BigDecimal)}
 * references the {@code test()} method in the {@code org.example.MyTests}
 * class that requires a {@code String} and {@code BigDecimal} as parameters.
 *
 * @author Sam Brannen
 * @since 5.0
 */
@Data
@EqualsAndHashCode
public class JavaTestDescriptor implements TestDescriptor {
  // The following pattern only supports descriptors for test methods.
  // TODO Support descriptors for test classes.
  // TODO Decide if we want to support descriptors for packages.
  private static final Pattern UID_PATTERN = Pattern.compile("^(.+):(.+)#(.+)\\((.*)\\)$");

  public static JavaTestDescriptor from(final String uid) throws RuntimeException {
    Preconditions.notNull(uid, "TestDescriptor UID must not be null");

    Matcher matcher = UID_PATTERN.matcher(uid);
    Preconditions.condition(matcher.matches(), () ->
        String.format("Invalid format for %s UID: %s", JavaTestDescriptor.class.getSimpleName(), uid));

    // TODO Validate contents of matched groups.
    String engineId = matcher.group(1);
    String className = matcher.group(2);
    String methodName = matcher.group(3);
    String methodParameters = matcher.group(4);

    Class<?> clazz = loadClass(className);

    System.out.println("DEBUG - method params: " + methodParameters);

    List<Class<?>> paramTypeList = new ArrayList<>();
    for (String type : methodParameters.split(",")) {
      type = type.trim();
      if (!type.isEmpty()) {
        paramTypeList.add(loadClass(type));
      }
    }

    Class<?>[] parameterTypes = paramTypeList.toArray(new Class<?>[paramTypeList.size()]);

    try {
      Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
      return new JavaTestDescriptor(engineId, method);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Failed to get method with name '" + methodName + "'.", e);
    }
  }

  private final String uniqueId;
  private final String engineId;
  private final String testId;
  private final String displayName;
  private final TestDescriptor parent;
  private final List<TestDescriptor> children;
  private final Class<?> testClass;

  private final Method testMethod;

  public JavaTestDescriptor(String engineId, Method testMethod) {
    this(engineId, testMethod.getDeclaringClass(), testMethod, null, null);
  }

  public JavaTestDescriptor(String engineId, Class<?> testClass, Method testMethod) {
    this(engineId, testClass, testMethod, null, null);
  }


  public JavaTestDescriptor(String engineId, Class<?> testClass, Method testMethod,
                            TestDescriptor parent, List<TestDescriptor> children) {

    Preconditions.notEmpty(engineId, "engineId must not be null or empty");
    if (testMethod == null) {
      Preconditions.notNull(testClass, "testClass must not be null");
    } else {
      Preconditions.notNull(testMethod, "testMethod must not be null");
      testClass = testMethod.getDeclaringClass();
    }

    this.testClass = testClass;
    this.testMethod = testMethod;
    this.displayName = determineDisplayName(testClass, testMethod);
    this.parent = parent;
    this.children = (children != null ? unmodifiableList(children) : emptyList());
    this.engineId = engineId;
    this.testId = createTestId(testClass, testMethod);
    this.uniqueId = this.engineId + ":" + this.testId;
  }

  private static Class<?> loadClass(String name) {
    try {
      // TODO Add support for primitive types and arrays.
      return JavaTestDescriptor.class.getClassLoader().loadClass(name);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Failed to load class with name '" + name + "'.", e);
    }
  }

  private static String createTestId(Class<?> testClass, Method testMethod) {
    return (testMethod != null ? String.format("%s#%s(%s)", testClass.getName(), testMethod.getName(),
        nullSafeToString(testMethod.getParameterTypes())) : testClass.getName());
  }

  private static String determineDisplayName(Class<?> testClass, Method testMethod) {
    if (testMethod != null) {
      Test test = testMethod.getAnnotation(Test.class);
      if (test != null) {
        String customName = test.name();
        if (!ObjectUtils.isEmpty(customName)) {
          return customName;
        }
      }
      return testMethod.getName();
    } else {
      return testClass.getSimpleName();
    }
  }
}