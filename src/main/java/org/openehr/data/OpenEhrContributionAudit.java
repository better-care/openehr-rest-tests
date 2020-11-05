/*
 * Copyright 2020-2021 Better Ltd (www.better.care)
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

import com.nedap.archie.rm.generic.PartyIdentified;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author Dusan Markovic
 */
public class OpenEhrContributionAudit implements Serializable, Cloneable {
    private static final long serialVersionUID = -5275431648934348306L;

    private PartyIdentified committer;

    @XmlElement(required = true)
    public PartyIdentified getCommitter() {
        return committer;
    }

    public void setCommitter(PartyIdentified committer) {
        this.committer = committer;
    }
}
