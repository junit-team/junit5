package example.registration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithParameterizedExtension("foo")
public class ParameterizedExtensionDemo {

    @Test
    void parameterIsPassedToExtension(String param) {
        assertEquals("foo", param);
    }
}
