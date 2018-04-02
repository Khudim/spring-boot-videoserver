package com.khudim.storage;

public interface IFileStorage {

    byte[] downloadFile(String fileName);

    byte[] downloadFile(String fileName, String[] ranges);

    boolean uploadFile(String file);

    StorageType getStorageType();
}
