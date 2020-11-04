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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.OffsetDateTime;

/**
 * @author Dusan Markovic
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OpenEhrTemplateDefinition {
    private String templateId;
    private String archetypeId;
    private OffsetDateTime createdTimestamp;
    private String concept;

    public OpenEhrTemplateDefinition() {
    }

    @XmlElement(name = "template_id", required = true)
    @JsonProperty("template_id")
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @XmlElement(name = "archetype_id", required = true)
    @JsonProperty("archetype_id")
    public String getArchetypeId() {
        return archetypeId;
    }

    public void setArchetypeId(String archetypeId) {
        this.archetypeId = archetypeId;
    }

    @XmlElement(name = "created_timestamp", required = true)
    @JsonProperty("created_timestamp")
    public OffsetDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(OffsetDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }
}
