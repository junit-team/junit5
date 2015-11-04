/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

public class OldSpecificationResolver {

//	private final Set<TestDescriptor> testDescriptors;
//	private final TestDescriptor root;
//
//	private final TestClassTester classTester = new TestClassTester();
//	private final TestMethodTester methodTester = new TestMethodTester();
//
//	public OldSpecificationResolver(Set testDescriptors, TestDescriptor root) {
//		this.testDescriptors = testDescriptors;
//		this.root = root;
//	}
//
//	public void resolveElement(TestPlanSpecificationElement element) {
//		if (element.getClass() == ClassNameSpecification.class) {
//			resolveClassNameSpecification((ClassNameSpecification) element);
//		}
//		if (element.getClass() == UniqueIdSpecification.class) {
//			resolveUniqueIdSpecification((UniqueIdSpecification) element);
//		}
//	}
//
//	private void resolveClassNameSpecification(ClassNameSpecification element) {
//		Class<?> clazz = null;
//		try {
//			clazz = loadClass(element.getClassName());
//		}
//		catch (ClassNotFoundException e) {
//			throwCannotResolveClassNameException(element.getClassName());
//		}
//
//		resolveClass(clazz, root, true);
//	}
//
//	private ClassTestDescriptor resolveClass(Class<?> clazz, TestDescriptor parent, boolean withChildren) {
//		if (!classTester.accept(clazz)) {
//			throwCannotResolveClassNameException(clazz.getName());
//		}
//		if (clazz.isMemberClass()) {
//			parent = resolveClass(clazz.getEnclosingClass(), parent, false);
//		}
//		ClassTestDescriptor descriptor = new ClassTestDescriptor(clazz, parent);
//		testDescriptors.add(descriptor);
//		if (withChildren) {
//			//Todo: pull code of ClassNameTestDescriptorResolver in
//			testDescriptors.addAll(new ClassNameTestDescriptorResolver().resolveChildren(descriptor,
//				new ClassNameSpecification(clazz.getName())));
//		}
//		return descriptor;
//	}
//
//	private void resolveUniqueIdSpecification(UniqueIdSpecification uniqueIdSpecification) {
//		UniqueIdParts uniqueIdParts = new UniqueIdParts(uniqueIdSpecification.getUniqueId());
//
//		String engineId = uniqueIdParts.pop();
//		if (!root.getUniqueId().equals(engineId)) {
//			throwCannotResolveUniqueIdException(uniqueIdSpecification.getUniqueId(), engineId);
//		}
//
//		resolveUniqueId(uniqueIdSpecification, root, uniqueIdParts);
//
//	}
//
//	private void resolveUniqueId(UniqueIdSpecification uniqueIdSpecification, TestDescriptor parent,
//			UniqueIdParts uniqueIdRest) {
//		String idPart = uniqueIdRest.pop();
//		if (idPart.isEmpty()) {
//			return;
//		}
//		switch (idPart.charAt(0)) {
//			case ':':
//				TestDescriptor classDescriptor = resolveClassByUniqueId(parent, idPart, uniqueIdRest);
//				resolveUniqueId(uniqueIdSpecification, classDescriptor, uniqueIdRest);
//				break;
//			case '$':
//				TestDescriptor classDescriptor2 = resolveClassByUniqueId(parent, idPart, uniqueIdRest);
//				resolveUniqueId(uniqueIdSpecification, classDescriptor2, uniqueIdRest);
//				break;
//			case '#':
//				TestDescriptor methodDescriptor = resolveMethodByUniqueId(parent, idPart, uniqueIdRest);
//				resolveUniqueId(uniqueIdSpecification, methodDescriptor, uniqueIdRest);
//				break;
//			default:
//				throwCannotResolveUniqueIdException(uniqueIdSpecification.getUniqueId(), idPart);
//		}
//	}
//
//	private TestDescriptor resolveMethodByUniqueId(TestDescriptor parent, String part, UniqueIdParts uniqueIdRest) {
//		MethodTestDescriptor methodDescriptor = getMethodTestDescriptor(parent, part);
//		testDescriptors.add(methodDescriptor);
//		return methodDescriptor;
//	}
//
//	private MethodTestDescriptor getMethodTestDescriptor(TestDescriptor parent, String idPart) {
//		String methodName = idPart.substring(1, idPart.length() - 2);
//		ClassTestDescriptor classDescriptor = (ClassTestDescriptor) parent;
//		Method method = null;
//		try {
//			method = classDescriptor.getTestClass().getDeclaredMethod(methodName, new Class[0]);
//			if (!methodTester.accept(method)) {
//				throwCannotResolveUniqueIdException(parent.getUniqueId() + idPart, idPart);
//			}
//		}
//		catch (NoSuchMethodException nsme) {
//			throwCannotResolveUniqueIdException(parent.getUniqueId() + idPart, idPart);
//		}
//		return new MethodTestDescriptor(method, classDescriptor);
//
//		//Todo: Parameters in test methods are not yet considered
//		//		List<Class<?>> paramTypeList = new ArrayList<>();
//		//		for (String type : methodParameters.split(",")) {
//		//			type = type.trim();
//		//			if (!type.isEmpty()) {
//		//				paramTypeList.add(ReflectionUtils.loadClass(type));
//		//			}
//		//		}
//
//	}
//
//	private TestDescriptor resolveClassByUniqueId(TestDescriptor parent, String part, UniqueIdParts rest) {
//		ClassTestDescriptor classDescriptor = getClassTestDescriptor(parent, part);
//		testDescriptors.add(classDescriptor);
//		if (rest.isEmpty()) {
//			resolveClassChildren(classDescriptor);
//		}
//		return classDescriptor;
//	}
//
//	private void resolveClassChildren(ClassTestDescriptor parent) {
//		List<Method> testMethodCandidates = findMethods(parent.getTestClass(), methodTester::accept,
//			AnnotationUtils.MethodSortOrder.HierarchyDown);
//
//		// @formatter:off
//		testDescriptors.addAll(testMethodCandidates.stream()
//			.map(method -> new MethodTestDescriptor(method, parent))
//			.filter(descriptor -> !descriptorAlreadyExists(descriptor.getUniqueId()))
//			.collect(toList()));
//		// @formatter:on
//	}
//
//	private ClassTestDescriptor getClassTestDescriptor(TestDescriptor parent, String uniqueIdPart) {
//		String className = uniqueIdPart.substring(1);
//		if (parent instanceof ClassTestDescriptor) {
//			className = ((ClassTestDescriptor) parent).getTestClass().getName() + uniqueIdPart;
//		}
//		Class<?> clazz = null;
//		try {
//			clazz = loadClass(className);
//		}
//		catch (ClassNotFoundException e) {
//			throwCannotResolveUniqueIdException(parent.getUniqueId() + uniqueIdPart, uniqueIdPart);
//		}
//		ClassTestDescriptor newDescriptor = new ClassTestDescriptor(clazz, parent);
//		ClassTestDescriptor existingDescriptor = (ClassTestDescriptor) descriptorByUniqueId(
//			newDescriptor.getUniqueId());
//		if (existingDescriptor != null) {
//			return existingDescriptor;
//		}
//		else {
//			return newDescriptor;
//		}
//	}
//
//	private boolean descriptorAlreadyExists(String uniqueId) {
//		return descriptorByUniqueId(uniqueId) != null;
//	}
//
//	private TestDescriptor descriptorByUniqueId(String uniqueId) {
//		for (TestDescriptor descriptor : testDescriptors) {
//			if (descriptor.getUniqueId().equals(uniqueId)) {
//				return descriptor;
//			}
//		}
//		return null;
//	}
//
//	private void throwCannotResolveUniqueIdException(String fullUniqueId, String uniqueIdPart) {
//		throw new IllegalArgumentException(
//			String.format("Cannot resolve part '%s' of unique id '%s'", uniqueIdPart, fullUniqueId));
//	}
//
//	private void throwCannotResolveClassNameException(String className) {
//		throw new IllegalArgumentException(String.format("Cannot resolve class of name '%s'", className));
//	}

}
