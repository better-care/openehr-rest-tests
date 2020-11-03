package org.openehr.utils;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Dusan Markovic
 */
public class LocatableUid implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("::");
    private final String uid;
    private final String systemId;
    private final int version;

    public LocatableUid(String uid, String systemId, int version) {
        this.uid = uid;
        this.systemId = systemId;
        this.version = version;
    }

    public LocatableUid(String complete) {
        if (complete == null) {
            throw new IllegalArgumentException("null");
        } else {
            String[] parts = SPLIT_PATTERN.split(complete);
            if (parts.length != 3) {
                throw new IllegalArgumentException(complete);
            } else {
                this.uid = parts[0];
                this.systemId = parts[1];

                try {
                    this.version = Integer.parseInt(parts[2]);
                } catch (NumberFormatException var4) {
                    throw new IllegalArgumentException(complete);
                }
            }
        }
    }

    public String getUid() {
        return this.uid;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public int getVersion() {
        return this.version;
    }

    public LocatableUid previous() {
        return this.version == 1 ? null : new LocatableUid(this.uid, this.systemId, this.version - 1);
    }

    public LocatableUid next() {
        return new LocatableUid(this.uid, this.systemId, this.version + 1);
    }

    public String toString() {
        return this.uid + "::" + this.systemId + "::" + this.version;
    }

    public static boolean isSimple(String uid) {
        return (Boolean)applyUid(uid, (x) -> {
            return false;
        }, (x) -> {
            return true;
        });
    }

    public static boolean isLocatable(String uid) {
        return !isSimple(uid);
    }

    public static <T> T applyUid(String uid, Function<LocatableUid, T> ifLocatable, Function<String, T> ifSimple) {
        if (uid.contains("::")) {
            LocatableUid locatableUid = new LocatableUid(uid);
            return ifLocatable.apply(locatableUid);
        } else {
            return ifSimple.apply(uid);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            LocatableUid that = (LocatableUid)o;
            if (this.version != that.version) {
                return false;
            } else {
                return Objects.equals(this.systemId, that.systemId) && Objects.equals(this.uid, that.uid);
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.uid.hashCode();
        result = 31 * result + this.systemId.hashCode();
        result = 31 * result + this.version;
        return result;
    }
}
