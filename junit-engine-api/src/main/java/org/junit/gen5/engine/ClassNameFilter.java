package org.junit.gen5.engine;

import lombok.Value;

@Value
public class ClassNameFilter implements ClassFilter {

	final private String regex;

	@Override
	public boolean acceptClass(Class<?> clazz) {
		return clazz.getSimpleName().matches(regex);
	}

	@Override
	public String getDescription() {
		return "Filter class names with regular expression: " + regex;
	}
}
