package org.openehr.data;

import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.PartyIdentified;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dusan Markovic
 */
@SuppressWarnings("rawtypes")
@XmlRootElement
@XmlSeeAlso({Composition.class,
                    PartyIdentified.class,
                    OpenEhrContributionAudit.class,
                    OpenEhrContributionVersion.class,
                    OpenEhrContributionVersion.OpenEhrCommitAudit.class})
public class OpenEhrContributionRequest implements Serializable, Cloneable {
    private static final long serialVersionUID = -5703337964934071501L;

    private List<OpenEhrContributionVersion> versions;
    private OpenEhrContributionAudit audit;

    @XmlElement(required = true)
    public List<OpenEhrContributionVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<OpenEhrContributionVersion> versions) {
        this.versions = versions;
    }

    @XmlElement(required = true)
    public OpenEhrContributionAudit getAudit() {
        return audit;
    }

    public void setAudit(OpenEhrContributionAudit audit) {
        this.audit = audit;
    }
}
