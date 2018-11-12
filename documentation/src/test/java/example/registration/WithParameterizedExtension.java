package example.registration;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ParameterizedExtension.class)
public @interface WithParameterizedExtension {
    String value();
}
