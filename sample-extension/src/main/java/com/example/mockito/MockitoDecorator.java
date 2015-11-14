package com.example.mockito;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.api.extension.ArgumentResolutionException;
import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.AnnotationUtils;

import static org.mockito.Mockito.*;


public class MockitoDecorator implements MethodArgumentResolver {

	Map<TestExecutionContext, Map<Class<?>, Object>> mocks = new HashMap<>();

	@Override
	public boolean supports(Parameter parameter) {
		return AnnotationUtils.findAnnotation(parameter, InjectMock.class).isPresent();
	}

	@Override
	public Object resolveArgument(Parameter parameter, TestExecutionContext testExecutionContext)  throws ArgumentResolutionException {
		Map contextMocks = mocksFor(testExecutionContext);
		Class<?> mockType = parameter.getType();
		Object aMock = contextMocks.get(mockType);
		if (aMock == null) {
			aMock = mock(mockType);
			contextMocks.put(mockType, aMock);
		}
		return aMock;
	}

	private Map<Class<?>, Object> mocksFor(TestExecutionContext context) {
		Map<Class<?>, Object> contextMocks = mocks.get(context);
		if (contextMocks == null) {
			contextMocks = new HashMap<>();
			mocks.put(context, contextMocks);
		}
		return contextMocks;
	}
}
