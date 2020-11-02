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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.net.URI;

/**
 * @author Dusan Markovic
 */
public class OpenEhrRequestMetaData implements Serializable, Cloneable {
    private static final long serialVersionUID = 1420213708370514680L;

    private URI href;
    private String type;
    private String schemaVersion;
    private DateTime created;
    private String generator;
    private String aqlExecuted;

    public OpenEhrRequestMetaData() {
        created = DateTime.now();
    }

    @XmlElement(name = "_href")
    @JsonProperty("_href")
    public URI getHref() {
        return href;
    }

    public void setHref(URI href) {
        this.href = href;
    }

    @XmlElement(name = "_type")
    @JsonProperty("_type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "_schema_version")
    @JsonProperty("_schema_version")
    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @XmlElement(name = "_created")
    @JsonProperty("_created")
    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    @XmlElement(name = "_generator")
    @JsonProperty("_generator")
    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    @XmlElement(name = "_executed_aql")
    @JsonProperty("_executed_aql")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getAqlExecuted() {
        return aqlExecuted;
    }

    public void setAqlExecuted(String aqlExecuted) {
        this.aqlExecuted = aqlExecuted;
    }
}
