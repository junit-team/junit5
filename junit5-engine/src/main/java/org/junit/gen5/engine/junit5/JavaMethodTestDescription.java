package org.junit.gen5.engine.junit5;

import org.junit.gen5.engine.EngineTestDescription;

public class JavaMethodTestDescription implements EngineTestDescription {

	private final String className;
	private final String methodName;

	public JavaMethodTestDescription(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getId() {
		return getClassName() + ":" + getMethodName();
	}

	public String getDisplayName() {
		return getClassName() + ":" + getMethodName();
	}

}
