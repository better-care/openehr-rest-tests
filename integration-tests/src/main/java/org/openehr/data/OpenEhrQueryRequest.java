package org.openehr.data;

import care.better.platform.model.adapter.MapParametersAdapter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
    @XmlJavaTypeAdapter(MapParametersAdapter.class)
    @JsonProperty(value = "query_parameters")
    public Map<String, Object> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, Object> queryParameters) {
        this.queryParameters = queryParameters;
    }

}
