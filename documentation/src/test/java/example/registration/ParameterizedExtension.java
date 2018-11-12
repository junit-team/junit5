package example.registration;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.AnnotatedElement;

public class ParameterizedExtension implements ParameterResolver {

    private final String parameter;

    public ParameterizedExtension(AnnotatedElement annotatedElement) {
        parameter = annotatedElement.getAnnotation(WithParameterizedExtension.class).value();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == String.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameter;
    }
}
