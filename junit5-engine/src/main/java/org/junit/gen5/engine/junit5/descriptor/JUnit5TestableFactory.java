package org.junit.gen5.engine.junit5.descriptor;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JUnit5TestableFactory {
  private static final String SEPARATORS = ":$#";
  private final JUnit5TestEngine testEngine;

  public JUnit5TestableFactory(JUnit5TestEngine testEngine) {
    this.testEngine = testEngine;
  }

  public JUnit5Testable fromUniqueId(String uniqueId) {
    Preconditions.notEmpty(uniqueId, "uniqueId must not be empty");
    List<String> parts = split(uniqueId);
    Preconditions.condition(parts.remove(0).equals(testEngine.getId()), "uniqueId must start with engineId");
    return createElement(uniqueId, parts);
  }

  private List<String> split(String uniqueId) {
    List<String> parts = new ArrayList<String>();
    String currentPart = "";
    for (char c : uniqueId.toCharArray()) {
      if (SEPARATORS.contains(Character.toString(c))) {
        parts.add(currentPart);
        currentPart = "";
      }
      currentPart += c;
    }
    parts.add(currentPart);
    return parts;
  }

  private JUnit5Testable createElement(String uniqueId, List<String> parts) {
    AnnotatedElement currentJavaElement = null;
    Class<?> currentJavaContainer = null;
    String head = parts.remove(0);
    while (true) {
      switch (head.charAt(0)) {
        case ':':
          currentJavaElement = findTopLevelClass(head);
          break;
        case '$':
          currentJavaContainer = (Class<?>) currentJavaElement;
          currentJavaElement = findNestedClass(head, (Class<?>) currentJavaElement);
          break;
        case '#':
          currentJavaContainer = (Class<?>) currentJavaElement;
          currentJavaElement = findMethod(head, currentJavaContainer, uniqueId);
          break;
        default:
          currentJavaContainer = null;
          currentJavaElement = null;
      }

      if (currentJavaElement == null) {
        throwCannotResolveUniqueIdException(uniqueId, head);
      }
      if (parts.isEmpty())
        break;
      head = parts.remove(0);
    }
    if (currentJavaElement instanceof Method) {
      return new JUnit5Method(uniqueId, (Method) currentJavaElement, currentJavaContainer);
    }
    if (currentJavaElement instanceof Class) {
      return new JUnit5Class(uniqueId, (Class<?>) currentJavaElement);
    }
    return null; //cannot happen
  }

  private Method findMethod(String methodSpecPart, Class<?> clazz, String uniqueId) {
    int startParams = methodSpecPart.indexOf('(');
    String methodName = methodSpecPart.substring(1, startParams);
    int endParams = methodSpecPart.lastIndexOf(')');
    String paramsPart = methodSpecPart.substring(startParams + 1, endParams);
    Class<?>[] parameterTypes = findParameterTypes(paramsPart, uniqueId);
    return findMethod(clazz, methodName, parameterTypes);
  }

  private Class<?>[] findParameterTypes(String paramsPart, String uniqueId) {
    if (paramsPart.isEmpty()) {
      return new Class<?>[0];
    }
    List<Class<?>> types = Arrays.stream(paramsPart.split(",")).map(typeName -> {
      Optional<Class<?>> aClass = ReflectionUtils.loadClass(typeName);
      if (aClass.isPresent())
        return aClass.get();
      else {
        throwCannotResolveUniqueIdException(uniqueId, paramsPart);
        return null; //cannot happen
      }
    }).collect(Collectors.toList());
    return types.toArray(new Class<?>[types.size()]);
  }

  private Method findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
    return ReflectionUtils.findMethod(clazz, methodName, parameterTypes).orElseThrow(
        () -> new IllegalArgumentException(String.format("No method with 'name' %s and paramter types '%s'",
            methodName, Arrays.toString(parameterTypes))));
  }

  private Class<?> findNestedClass(String nameExtension, Class<?> containerClass) {
    String fullClassName = containerClass.getName() + nameExtension;
    return classByName(fullClassName);
  }

  private Class<?> findTopLevelClass(String classNamePart) {
    String className = classNamePart.substring(1);
    return classByName(className);
  }

  private Class<?> classByName(String className) {
    return ReflectionUtils.loadClass(className).orElse(null);
  }

  private void throwCannotResolveClassNameException(String className) {
    throw new IllegalArgumentException(String.format("Cannot resolve class name '%s'", className));
  }

  private void throwCannotResolveUniqueIdException(String fullUniqueId, String uniqueIdPart) {
    throw new IllegalArgumentException(
        String.format("Cannot resolve part '%s' of unique id '%s'", uniqueIdPart, fullUniqueId));
  }

  public JUnit5Testable fromClassName(String className) {
    Preconditions.notEmpty(className, "className must not be empty");
    Class<?> clazz = classByName(className);
    if (clazz == null) {
      throwCannotResolveClassNameException(className);
    }
    return fromClass(clazz);
  }

  public JUnit5Testable fromClass(Class<?> clazz) {
    Preconditions.notNull(clazz, "clazz must not be null");
    String uniqueId = testEngine.getId() + ":" + clazz.getName();
    return new JUnit5Class(uniqueId, clazz);
  }

  public JUnit5Testable fromMethod(Method testMethod, Class<?> clazz) {
    String uniqueId = fromClass(clazz).getUniqueId() + "#" + testMethod.getName()
        + getParameterIdPart(testMethod);
    return new JUnit5Method(uniqueId, testMethod, clazz);
  }

  private String getParameterIdPart(Method testMethod) {
    String parameterString = Arrays.stream(testMethod.getParameterTypes()).map(Class::getName).collect(
        Collectors.joining(","));
    return "(" + parameterString + ")";
  }
}