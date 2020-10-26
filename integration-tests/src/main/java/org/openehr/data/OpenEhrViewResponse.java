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
