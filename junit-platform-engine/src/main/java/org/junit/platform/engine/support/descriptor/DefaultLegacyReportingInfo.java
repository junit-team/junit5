package org.junit.platform.engine.support.descriptor;

import java.io.Serializable;
import java.util.Optional;
import org.junit.platform.engine.TestDescriptor;

public class DefaultLegacyReportingInfo implements TestDescriptor.LegacyReportingInfo, Serializable {
    private static final long serialVersionUID = 2896941058543948169L;
    private final String methodName;
    private final String className;


    public static DefaultLegacyReportingInfo from(TestDescriptor.LegacyReportingInfo info) {
        final String methodName = info.getMethodName().orElse(null);
        final String className = info.getClassName().orElse(null);
        return new DefaultLegacyReportingInfo(methodName, className);
    }

    public DefaultLegacyReportingInfo(String methodName, String className) {
        this.methodName = methodName;
        this.className = className;
    }

    @Override
    public Optional<String> getMethodName() {
        return Optional.ofNullable(methodName);
    }

    @Override
    public Optional<String> getClassName() {
        return Optional.ofNullable(className);
    }
}
