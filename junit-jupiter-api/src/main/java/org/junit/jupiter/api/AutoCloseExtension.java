/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code AutoCloseExtension} is a JUnit Jupiter extension that closes resources
 * if a field in a test class is annotated with {@link AutoClose @AutoClose}.
 *
 * <p>Consult the Javadoc for {@link AutoClose @AutoClose} for details on the
 * contract.
 *
 * @since 5.11
 * @see AutoClose
 */
class AutoCloseExtension implements AfterAllCallback, TestInstancePreDestroyCallback {

	private static final Logger logger = LoggerFactory.getLogger(AutoCloseExtension.class);
	private static final Namespace NAMESPACE = Namespace.create(AutoClose.class);

	@Override
	public void afterAll(ExtensionContext context) {
		Store contextStore = context.getStore(NAMESPACE);
		Class<?> testClass = context.getRequiredTestClass();

		registerCloseables(contextStore, testClass, null);
	}

	@Override
	public void preDestroyTestInstance(ExtensionContext context) {
		Store contextStore = context.getStore(NAMESPACE);

		for (Object instance : context.getRequiredTestInstances().getAllInstances()) {
			registerCloseables(contextStore, instance.getClass(), instance);
		}
	}

	private void registerCloseables(Store contextStore, Class<?> testClass, Object testInstance) {
		Predicate<Field> predicate = testInstance == null ? ReflectionUtils::isStatic : ReflectionUtils::isNotStatic;
		findAnnotatedFields(testClass, AutoClose.class, predicate).forEach(field -> {
			try {
				contextStore.put(field, asCloseableResource(testInstance, field));
			}
			catch (Throwable t) {
				throw ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	private static Store.CloseableResource asCloseableResource(Object testInstance, Field field) {
		return () -> {
			Object toBeClosed = ReflectionUtils.tryToReadFieldValue(field, testInstance).get();
			if (toBeClosed == null) {
				logger.warn(() -> "@AutoClose couldn't close object for field " + getQualifiedFieldName(field)
						+ "  because it was null.");
				return;
			}
			invokeCloseMethod(field, toBeClosed);
		};
	}

	private static void invokeCloseMethod(Field field, Object toBeClosed) {
		String methodName = field.getAnnotation(AutoClose.class).value();
		Method closeMethod = ReflectionUtils.findMethod(toBeClosed.getClass(), methodName).orElseThrow(
			() -> new ExtensionConfigurationException(
				"@AutoClose failed to close object for field " + getQualifiedFieldName(field) + " because the "
						+ methodName + "() method could not be " + "resolved."));
		ReflectionUtils.invokeMethod(closeMethod, toBeClosed);
	}

	private static String getQualifiedFieldName(Field field) {
		return field.getDeclaringClass().getName() + "." + field.getName();
	}

}
