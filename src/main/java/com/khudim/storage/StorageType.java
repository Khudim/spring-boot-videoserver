package com.khudim.storage;

public enum StorageType {
    DROP_BOX("DROP_BOX"),
    GOOGLE_DRIVE("GOOGLE_DRIVE"),
    LOCAL_STORAGE("LOCAL_STORAGE");

    private String storage;

    StorageType(String storage) {
        this.storage = storage;
    }
}
