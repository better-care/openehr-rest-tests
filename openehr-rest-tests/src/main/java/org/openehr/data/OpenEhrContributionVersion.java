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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.nedap.archie.rm.generic.PartyProxy;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author Dusan Markovic
 */
public class OpenEhrContributionVersion implements Serializable, Cloneable {
    private static final long serialVersionUID = 2189225155652394124L;
    private JsonNode data;
    private String precedingVersionUid;
    private String signature;
    private int lifecycleState;
    private OpenEhrCommitAudit commitAudit;

    @XmlElement(required = true)
    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
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
        private String changeType;
        private String description;
        private PartyProxy committer;

        @XmlElement(required = true, name = "change_type")
        @JsonProperty("change_type")
        public String getChangeType() {
            return changeType;
        }

        public void setChangeType(String changeType) {
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
        public PartyProxy getCommitter() {
            return committer;
        }

        public void setCommitter(PartyProxy committer) {
            this.committer = committer;
        }
    }
}
