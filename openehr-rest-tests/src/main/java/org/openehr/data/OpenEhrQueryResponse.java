package org.openehr.data;

import care.better.platform.model.Ehr;
import care.better.platform.model.ListResultColumn;
import care.better.platform.model.ListResultRow;
import care.better.platform.model.ListResultSet;
import care.better.platform.model.VersionedObjectDto;
import care.better.platform.model.adapter.ListResultSetAdapter;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.OriginalVersion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dusan Markovic
 */
@XmlRootElement
@XmlSeeAlso({
                    OpenEhrRequestMetaData.class,
                    OpenEhrQueryResponse.OpenEhrColumnData.class,
                    ListResultSet.class,
                    ListResultRow.class,
                    ListResultColumn.class,
                    Composition.class,
                    Ehr.class,
                    VersionedObjectDto.class,
                    OriginalVersion.class})
@XmlAccessorType(XmlAccessType.NONE)
public class OpenEhrQueryResponse implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private OpenEhrRequestMetaData meta;
    private String name;
    private String q;
    private List<OpenEhrColumnData> columns;
    private List<List<Object>> rows;

    @XmlElement
    public OpenEhrRequestMetaData getMeta() {
        return meta;
    }

    public void setMeta(OpenEhrRequestMetaData meta) {
        this.meta = meta;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    @XmlElement
    public List<OpenEhrColumnData> getColumns() {
        return columns;
    }

    public void setColumns(List<OpenEhrColumnData> columns) {
        this.columns = columns;
    }

    @XmlAnyElement
    @XmlJavaTypeAdapter(ListResultSetAdapter.class)
    public List<List<Object>> getRows() {
        return rows;
    }

    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }

    @XmlRootElement
    @XmlAccessorType
    public static class OpenEhrColumnData implements Serializable, Cloneable {
        private static final long serialVersionUID = 2189486644471619461L;
        private String name;
        private String path;

        public OpenEhrColumnData() {
        }

        public OpenEhrColumnData(String name, String path) {
            this.name = name;
            this.path = path;
        }

        @XmlElement
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
