package org.junit.platform.reporting.open.xml;

import org.opentest4j.reporting.events.api.Factory;
import org.opentest4j.reporting.events.api.Namespace;

public class JUnitFactory {

    public static Namespace NAMESPACE = Namespace.of("https://schemas.opentest4j.org/reporting/junit/1.0");

    private JUnitFactory() {
    }

    public static Factory<LegacyReportingName> legacyReportingName(String legacyReportingName) {
        return context -> new LegacyReportingName(context, legacyReportingName);
    }

    public static Factory<Type> type(String value) {
        return context -> new Type(context, value);
    }
}
