package org.junit.gen5.commons.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class ReflectionUtils {
  private ReflectionUtils() {
		/* no-op */
  }

  public static <T extends Object> T newInstance(Class<T> clazz)
      throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
    Constructor<T> constructor = clazz.getDeclaredConstructor();
    if (!constructor.isAccessible()) {
      constructor.setAccessible(true);
    }
    return constructor.newInstance();
  }

  public static Object invokeMethod(Method method, Object testInstance)
      throws IllegalAccessException, InvocationTargetException {
    if (!method.isAccessible()) {
      method.setAccessible(true);
    }
    return method.invoke(testInstance);
  }
}