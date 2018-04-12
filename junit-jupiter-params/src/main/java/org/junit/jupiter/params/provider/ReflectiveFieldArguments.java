package org.junit.jupiter.params.provider;

import java.util.Arrays;

/**
 * helper class that can produce arguments based on the object field order. 
 */
public abstract class ReflectiveFieldArguments implements Arguments {

	@Override
	public Object[] get() {
		return Arrays.stream(this.getClass().getDeclaredFields()).filter(f -> !f.isSynthetic()).map(f -> {
			f.setAccessible(true);
			try {
				return f.get(this);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Error while getting value for field '" + f.getName() + "' in " + this, e);
			}
		}).toArray(Object[]::new);
	}
}
