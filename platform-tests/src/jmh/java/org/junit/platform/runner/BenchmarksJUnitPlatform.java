package org.junit.platform.runner;

import org.junit.platform.launcher.Launcher;

public class BenchmarksJUnitPlatform extends JUnitPlatform {
    public BenchmarksJUnitPlatform(Class<?> testClass, Launcher launcher) {
        super(testClass, launcher);
    }
}
