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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Dusan Markovic
 */
@XmlRootElement
public class OpenEhrQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String q;
    private Integer offset;
    private Integer fetch;
    private Map<String, Object> queryParameters;

    @XmlElement
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    @XmlElement
    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @XmlElement
    public Integer getFetch() {
        return fetch;
    }

    public void setFetch(Integer fetch) {
        this.fetch = fetch;
    }

    @XmlElement(name = "query_parameters")
    @JsonProperty(value = "query_parameters")
    public Map<String, Object> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, Object> queryParameters) {
        this.queryParameters = queryParameters;
    }

}
