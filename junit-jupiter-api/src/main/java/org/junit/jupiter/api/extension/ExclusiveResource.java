package org.junit.jupiter.api.extension;

import org.junit.jupiter.api.parallel.ResourceAccessMode;

public interface ExclusiveResource {

    String getKey();

    ResourceAccessMode getAccessMode();

    static ExclusiveResource of(String key) {
        return null;
    }

    static ExclusiveResource of(String key, ResourceAccessMode accessMode) {
        return null;
    }
}
