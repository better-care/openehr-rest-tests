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

import com.google.common.base.Preconditions;
import org.openehr.utils.LocatableUid;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Matic Ribic
 */
public class OpenEhrUri {
    public static final String URL_HEADER_NAME = "openEHR-uri";
    private final String ehrId;
    private final OpenEhrStructureType topLevelStructureType;
    private final String topLevelUid;

    public OpenEhrUri(@Nonnull String ehrId, OpenEhrStructureType topLevelStructureType, String topLevelUid) {
        Preconditions.checkNotNull(ehrId);
        this.ehrId = ehrId;
        this.topLevelStructureType = topLevelStructureType;
        this.topLevelUid = topLevelUid != null ? LocatableUid.applyUid(topLevelUid, LocatableUid::getUid, Function.identity()) : null;
    }

    public String getEhrId() {
        return ehrId;
    }

    public OpenEhrStructureType getTopLevelStructureType() {
        return topLevelStructureType;
    }

    public String getTopLevelUid() {
        return topLevelUid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ehrId, topLevelStructureType, topLevelUid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpenEhrUri)) {
            return false;
        }

        OpenEhrUri otherEhrUri = (OpenEhrUri)o;
        return Objects.equals(ehrId, otherEhrUri.ehrId)
                && topLevelStructureType == otherEhrUri.topLevelStructureType
                && Objects.equals(topLevelUid, otherEhrUri.topLevelUid);
    }

    @Override
    public String toString() {
        return OpenEhrUriFormat.format(this);
    }
}
