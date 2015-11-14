package com.example.mockito;

import java.lang.reflect.Parameter;

import org.junit.gen5.api.extension.ArgumentResolutionException;
import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestExecutionContext;

public class MockitoDecorator implements MethodArgumentResolver {

	@Override
	public boolean supports(Parameter parameter) {
		return false;
	}

	@Override
	public Object resolveArgument(Parameter parameter, TestExecutionContext testExecutionContext)
			throws ArgumentResolutionException {
		return null;
	}
}
