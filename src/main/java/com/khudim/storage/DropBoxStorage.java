package com.khudim.storage;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.khudim.utils.VideoHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang.math.NumberUtils.toInt;

@Component
public class DropBoxStorage implements IFileStorage {

    private final static Map<String, DbxDownloader<FileMetadata>> CASH = new ConcurrentHashMap<>();

    @Value("dropBox.max_cash_size")
    private final static int MAX_CASH_SIZE = 300;

    @Value("dropBox.token")
    private String accessToken;

    @Value("dropBox.identifier")
    private String clientIdentifier;

    private DbxClientV2 client;

    public DropBoxStorage() {
        DbxRequestConfig config = new DbxRequestConfig(clientIdentifier);
        client = new DbxClientV2(config, accessToken);
    }

    @Override
    public String storageName() {
        return "DropBox";
    }

    @Override
    public byte[] downloadFile(String fileName, String[] ranges) throws Exception {
        int offset = toInt(ranges[0]);
        int limit = toInt(ranges[1]);
        DbxDownloader<FileMetadata> meta = CASH.get(fileName);
        if (meta == null) {
            if (CASH.size() > MAX_CASH_SIZE) {
                CASH.forEach((k, v) -> v.close());
                CASH.clear();
            }
            meta = client.files().download(fileName);
            CASH.put(fileName, meta);
        }
        byte[] bytes = new byte[limit - offset + 1];
        meta.getInputStream().readNBytes(bytes, offset, limit);
        return bytes;
    }

    public boolean uploadFile(String file) {
        try (InputStream in = new FileInputStream(file)) {
            client.files().uploadBuilder(VideoHelper.getNameFromPath(file))
                    .uploadAndFinish(in);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
