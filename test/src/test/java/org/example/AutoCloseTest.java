package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AutoCloseExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AutoCloseExtension.class)
class AutoCloseTest {
    @AutoClose
    private Closeable resource = createResource();

    @Test
    void testResourceIsClosed() throws IOException {
        Assertions.assertTrue(resource instanceof Closeable);
    }

    private Closeable createResource() {
        return new Closeable() {
            @Override
            public void close() throws IOException {
                System.out.println("Closing resource...");
            }
        };
    }
}
