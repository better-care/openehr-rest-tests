package org.openehr.data;

import org.openehr.jaxb.rm.PartyIdentified;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author Dusan Markovic
 */
public class OpenEhrContributionAudit implements Serializable, Cloneable {
    private static final long serialVersionUID = -5275431648934348306L;

    private PartyIdentified committer;

    @XmlElement(required = true)
    public PartyIdentified getCommitter() {
        return committer;
    }

    public void setCommitter(PartyIdentified committer) {
        this.committer = committer;
    }
}
