package com.khudim.storage;

public interface IFileStorage {

    byte[] downloadFile(String fileName);

    byte[] downloadFile(String fileName, int offset, int limit);

    boolean uploadFile(String file);

    StorageType getStorageType();
}
