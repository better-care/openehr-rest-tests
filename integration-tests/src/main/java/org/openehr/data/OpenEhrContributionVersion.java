package org.openehr.data;

import care.better.platform.openehr.rm.RmObject;
import care.better.platform.service.AuditChangeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openehr.jaxb.rm.PartyIdentified;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author Dusan Markovic
 */
public class OpenEhrContributionVersion<T extends RmObject> implements Serializable, Cloneable {
    private static final long serialVersionUID = 2189225155652394124L;
    private T data;
    private String precedingVersionUid;
    private String signature;
    private int lifecycleState;
    private OpenEhrCommitAudit commitAudit;

    @XmlElement(required = true)
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @XmlElement(name = "preceding_version_uid")
    @JsonProperty("preceding_version_uid")
    public String getPrecedingVersionUid() {
        return precedingVersionUid;
    }

    public void setPrecedingVersionUid(String precedingVersionUid) {
        this.precedingVersionUid = precedingVersionUid;
    }

    @XmlElement
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @XmlElement(name = "lifecycle_state")
    @JsonProperty("lifecycle_state")
    public int getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(int lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    @XmlElement(required = true, name = "commit_audit")
    @JsonProperty("commit_audit")
    public OpenEhrCommitAudit getCommitAudit() {
        return commitAudit;
    }

    public void setCommitAudit(OpenEhrCommitAudit commitAudit) {
        this.commitAudit = commitAudit;
    }

    public static class OpenEhrCommitAudit implements Serializable, Cloneable {
        private static final long serialVersionUID = 4508719136393176130L;
        private AuditChangeType changeType;
        private String description;
        private PartyIdentified committer;

        @XmlElement(required = true, name = "change_type")
        @JsonProperty("change_type")
        public AuditChangeType getChangeType() {
            return changeType;
        }

        public void setChangeType(AuditChangeType changeType) {
            this.changeType = changeType;
        }

        @XmlElement
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @XmlElement
        public PartyIdentified getCommitter() {
            return committer;
        }

        public void setCommitter(PartyIdentified committer) {
            this.committer = committer;
        }
    }
}
