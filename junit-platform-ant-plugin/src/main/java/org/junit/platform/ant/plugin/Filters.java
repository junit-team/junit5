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

public class Filters {

	private List<String> includeClassNamePatterns = new ArrayList<String>();

	private FilterSet packages;
	private FilterSet engines;
	private FilterSet tags;

	public void setIncludeClassNamePattern(String includeClassNamePatterns) {
		setIncludeClassNamePatterns(includeClassNamePatterns);
	}

	public void setIncludeClassNamePatterns(String includeClassNamePatterns) {
		tokenize(this.includeClassNamePatterns::add, includeClassNamePatterns);
	}

	public List<String> getIncludeClassNamePatterns() {
		return this.includeClassNamePatterns;
	}

	public FilterSet createPackages() {
		if (packages == null) {
			packages = new FilterSet();
		}
		return packages;
	}

	public FilterSet getPackages() {
		return this.packages;
	}

	public FilterSet createEngines() {
		if (engines == null) {
			engines = new FilterSet();
		}
		return engines;
	}

	public FilterSet getEngines() {
		return this.engines;
	}

	public FilterSet createTags() {
		if (tags == null) {
			tags = new FilterSet();
		}
		return tags;
	}

	public FilterSet getTags() {
		return this.tags;
	}

	private void tokenize(Consumer<String> element, String elementValue) {
		StringTokenizer tokenizer = new StringTokenizer(elementValue, ",", false);
		while (tokenizer.hasMoreTokens()) {
			element.accept(tokenizer.nextToken().trim());
		}
	}

	public class FilterSet {
		private List<String> includes = new ArrayList<String>();
		private List<String> excludes = new ArrayList<String>();

		public void setInclude(String includes) {
			tokenize(this.includes::add, includes);
		}

		public List<String> getInclude() {
			return this.includes;
		}

		public void setExclude(String excludes) {
			tokenize(this.excludes::add, excludes);
		}

		public List<String> getExclude() {
			return this.excludes;
		}
	}
}
