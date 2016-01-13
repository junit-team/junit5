package example;

// tag::user_guide[]
import org.junit.gen5.api.*;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("fast")
public @interface Fast {
}
// end::user_guide[]
