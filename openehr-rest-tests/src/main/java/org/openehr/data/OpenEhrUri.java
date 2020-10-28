package org.openehr.data;

import care.better.platform.locatable.LocatableUid;
import com.google.common.base.Preconditions;

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
