package com.khudim.storage;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.khudim.storage.StorageType.LOCAL_STORAGE;

@Component
@Data
@ConditionalOnProperty(prefix = "storage.local", value = "folder")
@ConfigurationProperties(prefix = "storage.local")
public class LocalStorage implements IFileStorage {
    private final static Logger log = LoggerFactory.getLogger(LocalStorage.class);
    private StorageType storageType = LOCAL_STORAGE;
    private String folder;

    @Override
    public byte[] downloadFile(String file) {
        try {
            return Files.readAllBytes(Paths.get(file));
        } catch (IOException e) {
            log.debug("Can't download file from {}, reason: {}.", storageType, e.getCause());
        }
        return new byte[0];
    }

    @Override
    public byte[] downloadFile(String file, int offset, int limit) {
        byte[] bytes = new byte[limit - offset + 1];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(new File(file), "rw")) {
            randomAccessFile.seek(offset);
            randomAccessFile.readFully(bytes, 0, limit - offset);
        } catch (IOException e) {
            log.debug("Can't download file from {}, reason: {}.", storageType, e.getCause());
        }
        return bytes;
    }

    @Override
    public boolean uploadFile(String file) {
        return false;
    }
}
