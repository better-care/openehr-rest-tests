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
