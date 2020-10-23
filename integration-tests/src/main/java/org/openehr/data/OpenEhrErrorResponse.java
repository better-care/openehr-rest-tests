package org.openehr.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dusan Markovic
 */
@XmlRootElement
public class OpenEhrErrorResponse implements Serializable, Cloneable {
    private static final long serialVersionUID = -6403176022804125919L;

    private String message;
    private List<String> validationErrors;

    public OpenEhrErrorResponse() {
    }

    public OpenEhrErrorResponse(String message) {
        this.message = message;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement(name = "validation_errors")
    @JsonProperty("validation_errors")
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
