package com.khudim.storage;

public interface IFileStorage {

    byte[] downloadFile(String fileName);

    byte[] downloadFile(String fileName, int offset, int limit) throws Exception;

    boolean uploadFile(String file);

    StorageType getStorageType();
}
