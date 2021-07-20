package org.junit.jupiter.engine.descriptor;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

@FunctionalInterface
@API(
    status = Status.STABLE,
    since = "5.0"
)
public interface BeforeAllClassesCallback extends Extension {
    default Integer getBeforeCallbackExecutionOrder() {
        return 0;
    };
    void beforeAllClasses(ExtensionContext var1) throws Exception;
}