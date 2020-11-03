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

import com.nedap.archie.rm.generic.PartyProxy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dusan Markovic
 */
@SuppressWarnings("rawtypes")
@XmlRootElement
@XmlSeeAlso({PartyProxy.class,
        OpenEhrContributionAudit.class,
        OpenEhrContributionVersion.class,
        OpenEhrContributionVersion.OpenEhrCommitAudit.class})
public class OpenEhrContributionRequest implements Serializable, Cloneable {
    private static final long serialVersionUID = -5703337964934071501L;

    private List<OpenEhrContributionVersion> versions;
    private OpenEhrContributionAudit audit;

    @XmlElement(required = true)
    public List<OpenEhrContributionVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<OpenEhrContributionVersion> versions) {
        this.versions = versions;
    }

    @XmlElement(required = true)
    public OpenEhrContributionAudit getAudit() {
        return audit;
    }

    public void setAudit(OpenEhrContributionAudit audit) {
        this.audit = audit;
    }
}
