package org.junit.platform.reporting.open.xml;

import org.opentest4j.reporting.events.api.ChildElement;
import org.opentest4j.reporting.events.api.Context;
import org.opentest4j.reporting.events.api.QualifiedName;
import org.opentest4j.reporting.events.core.Metadata;

public class Type extends ChildElement<Metadata, Type> {

    public static final QualifiedName ELEMENT = QualifiedName.of(JUnitFactory.NAMESPACE, "type");

    Type(Context context, String value) {
        super(context, ELEMENT);
        withContent(value);
    }
}
