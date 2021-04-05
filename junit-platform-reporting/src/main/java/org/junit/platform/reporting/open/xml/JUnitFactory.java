package org.junit.platform.reporting.open.xml;

import org.junit.platform.engine.TestDescriptor;
import org.opentest4j.reporting.events.api.Factory;
import org.opentest4j.reporting.events.api.Namespace;

class JUnitFactory {

    public static Namespace NAMESPACE = Namespace.of("https://schemas.opentest4j.org/reporting/junit/1.0");

    private JUnitFactory() {
    }

    static Factory<LegacyReportingName> legacyReportingName(String legacyReportingName) {
        return context -> new LegacyReportingName(context, legacyReportingName);
    }

    static Factory<Type> type(TestDescriptor.Type type) {
        return context -> new Type(context, type);
    }
}
