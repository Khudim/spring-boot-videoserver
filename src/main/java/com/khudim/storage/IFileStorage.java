package com.khudim.storage;

public interface IFileStorage {

    byte[] downloadFile(String fileName, String[] ranges) throws Exception;

    String storageName();
}
