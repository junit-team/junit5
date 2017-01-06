/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.ant.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;

public class Selectors {

	private List<String> uris = new ArrayList<String>();
	private List<String> files = new ArrayList<String>();
	private List<String> directories = new ArrayList<String>();
	private List<String> packages = new ArrayList<String>();
	private List<String> classes = new ArrayList<String>();
	private List<String> methods = new ArrayList<String>();
	private List<String> resources = new ArrayList<String>();

	public void setUri(String uri) {
		setUris(uri);
	}

	public void setUris(String uris) {
		tokenize(this.uris::add, uris);
	}

	public List<String> getUris() {
		return this.uris;
	}

	public void setFile(String file) {
		setFiles(file);
	}

	public void setFiles(String files) {
		tokenize(this.files::add, files);
	}

	public List<String> getFiles() {
		return this.files;
	}

	public void setDirectory(String dir) {
		setDirectories(dir);
	}

	public void setDirectories(String dirs) {
		tokenize(this.directories::add, dirs);
	}

	public List<String> getDirectories() {
		return this.directories;
	}

	public void setPackage(String pkg) {
		setPackages(pkg);
	}

	public void setPackages(String packages) {
		tokenize(this.packages::add, packages);
	}

	public List<String> getPackages() {
		return this.packages;
	}

	public void setClass(String clazz) {
		setClasses(clazz);
	}

	public void setClasses(String classes) {
		tokenize(this.classes::add, classes);
	}

	public List<String> getClasses() {
		return this.classes;
	}

	public void setMethod(String method) {
		setMethods(method);
	}

	public void setMethods(String methods) {
		tokenize(this.methods::add, methods);
	}

	public List<String> getMethods() {
		return this.methods;
	}

	public void setResource(String rsc) {
		setResources(rsc);
	}

	public void setResources(String rscs) {
		tokenize(this.resources::add, rscs);
	}

	public List<String> getResources() {
		return this.resources;
	}

	private void tokenize(Consumer<String> element, String elementValue) {
		StringTokenizer tokenizer = new StringTokenizer(elementValue, ",", false);
		while (tokenizer.hasMoreTokens()) {
			element.accept(tokenizer.nextToken().trim());
		}
	}
}
