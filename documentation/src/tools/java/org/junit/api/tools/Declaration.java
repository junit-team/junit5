/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.api.tools;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

sealed interface Declaration extends Comparable<Declaration> {

	String packageName();

	String fullName();

	String name();

	String kind();

	Status status();

	String since();

	@Override
	default int compareTo(Declaration o) {
		return fullName().compareTo(o.fullName());
	}

	record Type(ClassInfo classInfo) implements Declaration {

		@Override
		public String packageName() {
			return classInfo.getPackageName();
		}

		@Override
		public String fullName() {
			return classInfo.getName();
		}

		@Override
		public String name() {
			return getShortClassName(classInfo);
		}

		@Override
		public String kind() {
			return switch (classInfo) {
				case ClassInfo ignored when classInfo.isRecord() -> "record";
				case ClassInfo ignored when classInfo.isAnnotation() -> "annotation";
				case ClassInfo ignored when classInfo.isEnum() -> "enum";
				case ClassInfo ignored when classInfo.isInterface() -> "interface";
				default -> "class";
			};
		}

		@Override
		public Status status() {
			return readStatus(getParameterValues());
		}

		@Override
		public String since() {
			return readSince(getParameterValues());
		}

		private AnnotationParameterValueList getParameterValues() {
			return classInfo.getAnnotationInfo(API.class).getParameterValues();
		}
	}

	record Method(MethodInfo methodInfo) implements Declaration {

		@Override
		public String packageName() {
			return classInfo().getPackageName();
		}

		@Override
		public String fullName() {
			return "%s.%s".formatted(classInfo().getName(), methodSignature());
		}

		@Override
		public String name() {
			return "%s.%s".formatted(getShortClassName(classInfo()), methodSignature());
		}

		private String methodSignature() {
			var parameters = Arrays.stream(methodInfo.getParameterInfo()) //
					.map(parameterInfo -> parameterInfo.getTypeSignatureOrTypeDescriptor().toStringWithSimpleNames()) //
					.collect(joining(", ", "(", ")"));
			return methodInfo.getName() + parameters;
		}

		@Override
		public String kind() {
			if (methodInfo.isConstructor()) {
				return "constructor";
			}
			if (classInfo().isAnnotation()) {
				return "annotation attribute";
			}
			return "method";
		}

		@Override
		public Status status() {
			return readStatus(getParameterValues());
		}

		@Override
		public String since() {
			return readSince(getParameterValues());
		}

		private AnnotationParameterValueList getParameterValues() {
			return methodInfo.getAnnotationInfo(API.class).getParameterValues();
		}

		public ClassInfo classInfo() {
			return methodInfo.getClassInfo();
		}
	}

	private static Status readStatus(AnnotationParameterValueList parameterValues) {
		return Status.valueOf(((AnnotationEnumValue) parameterValues.getValue("status")).getValueName());
	}

	private static String readSince(AnnotationParameterValueList parameterValues) {
		return (String) parameterValues.getValue("since");
	}

	private static String getShortClassName(ClassInfo classInfo) {
		var typeName = classInfo.getName();
		var packageName = classInfo.getPackageName();
		if (typeName.startsWith(packageName + '.')) {
			typeName = typeName.substring(packageName.length() + 1);
		}
		return typeName;
	}
}
