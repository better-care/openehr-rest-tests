package org.openehr.data;

import java.util.stream.Stream;

/**
 * @author Matic Ribic
 */
public enum OpenEhrStructureType {
    COMPOSITION("compositions"),
    DIRECTORY("directory"),
    STATUS("status");

    private final String uriType;

    OpenEhrStructureType(String uriType) {
        this.uriType = uriType;
    }

    public static OpenEhrStructureType fromUriType(String uriType) {
        return Stream.of(OpenEhrStructureType.values())
                .filter(openEhrStructureType -> openEhrStructureType.uriType.equals(uriType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(uriType));
    }

    public String getUriType() {
        return uriType;
    }
}
