package org.openehr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openehr.jaxb.am.Template;

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

    public OpenEhrTemplateDefinition(String templateId, OffsetDateTime createdTimestamp, Template template) {
        this.templateId = templateId;
        this.createdTimestamp = createdTimestamp;
        concept = template.getConcept();
        archetypeId = template.getDefinition() == null || template.getDefinition().getArchetypeId() == null
                ? null
                : template.getDefinition().getArchetypeId().getValue();
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
