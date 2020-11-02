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

package org.openehr.utils;

import care.better.platform.util.ConversionUtils;
import org.openehr.jaxb.rm.Folder;
import org.openehr.jaxb.rm.ObjectRef;
import org.openehr.jaxb.rm.ObjectVersionId;

/**
 * @author Domen Muren
 */
public final class FolderBuilder {
    private final Folder rootFolder = new Folder();

    public static FolderBuilder createFolder() {
        return new FolderBuilder();
    }

    public Folder build() {
        return rootFolder;
    }

    public FolderBuilder withName(String name) {
        rootFolder.setName(ConversionUtils.getText(name));
        return this;
    }

    public FolderBuilder withUid(String uid) {
        ObjectVersionId objectVersionId = new ObjectVersionId();
        objectVersionId.setValue(uid);
        rootFolder.setUid(objectVersionId);
        return this;
    }

    public FolderBuilder withItems(ObjectRef... items) {
        for (ObjectRef item : items) {
            rootFolder.getItems().add(item);
        }
        return this;
    }

    public FolderBuilder withSubfolder(Folder subfolder) {
        rootFolder.getFolders().add(subfolder);
        return this;
    }

    private FolderBuilder() {
    }
}
