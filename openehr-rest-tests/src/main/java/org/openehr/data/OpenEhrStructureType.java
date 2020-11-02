/*
 * Copyright (C) 2020 Better d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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
