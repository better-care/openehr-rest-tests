/*
 * Copyright (C) 2020 Better d.o.o.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dusan Markovic
 */
@XmlRootElement
@XmlSeeAlso({
        OpenEhrRequestMetaData.class,
        OpenEhrQueryResponse.OpenEhrColumnData.class})
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
