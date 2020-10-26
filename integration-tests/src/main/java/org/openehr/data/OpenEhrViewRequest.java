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
public class OpenEhrViewRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String q;
    private String description;
    private Map<String, String> queryParameters;

    @XmlElement
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "query_parameters")
    @JsonProperty("query_parameters")
    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }
}
