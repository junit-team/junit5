
package org.junit.gen5.commons.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
// TODO Check if SOURCE is sufficient
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface API {

	Usage value();

	enum Usage {

		Deprecated,

		Internal,

		Experimental,

		Maintained,

		Stable

	}

}
