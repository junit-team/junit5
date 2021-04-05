package org.junit.platform.reporting.open.xml;

import org.opentest4j.reporting.events.api.ChildElement;
import org.opentest4j.reporting.events.api.Context;
import org.opentest4j.reporting.events.api.QualifiedName;
import org.opentest4j.reporting.events.core.Metadata;

public class LegacyReportingName extends ChildElement<Metadata, LegacyReportingName> {

    public static final QualifiedName ELEMENT = QualifiedName.of(JUnitFactory.NAMESPACE, "legacyReportingName");

    LegacyReportingName(Context context, String value) {
        super(context, ELEMENT);
        withContent(value);
    }
}
