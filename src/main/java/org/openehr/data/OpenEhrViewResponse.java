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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * @author Dusan Markovic
 */
@XmlRootElement
public class OpenEhrViewResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private String version;
    private OffsetDateTime saved;
    private String q;

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement
    public OffsetDateTime getSaved() {
        return saved;
    }

    public void setSaved(OffsetDateTime saved) {
        this.saved = saved;
    }

    @XmlElement
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }
}
