package org.openehr.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Dusan Markovic
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OpenEhrConformance {

    private String solution;
    @XmlElement(name = "solution_version")
    @JsonProperty("solution_version")
    private String solutionVersion;
    private String vendor;
    @XmlElement(name = "restapi_specs_version")
    @JsonProperty("restapi_specs_version")
    private String restapiSpecsVersion;
    @XmlElement(name = "conformance_profile")
    @JsonProperty("conformance_profile")
    private String conformanceProfile;
    @XmlElement(name = "created_timestamp")
    private List<String> endpoints;

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getSolutionVersion() {
        return solutionVersion;
    }

    public void setSolutionVersion(String solutionVersion) {
        this.solutionVersion = solutionVersion;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getRestapiSpecsVersion() {
        return restapiSpecsVersion;
    }

    public void setRestapiSpecsVersion(String restapiSpecsVersion) {
        this.restapiSpecsVersion = restapiSpecsVersion;
    }

    public String getConformanceProfile() {
        return conformanceProfile;
    }

    public void setConformanceProfile(String conformanceProfile) {
        this.conformanceProfile = conformanceProfile;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
}
