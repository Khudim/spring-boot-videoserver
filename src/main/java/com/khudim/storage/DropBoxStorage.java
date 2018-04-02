package com.khudim.storage;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.khudim.parser.HtmlParser;
import com.khudim.utils.VideoHelper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.khudim.storage.StorageType.DROP_BOX;
import static org.apache.commons.lang.math.NumberUtils.toInt;

@Component
@Data
@ConfigurationProperties(prefix = "drop-box")
public class DropBoxStorage implements IFileStorage {

    private final static Logger log = LoggerFactory.getLogger(HtmlParser.class);
    private final static Map<String, DbxDownloader<FileMetadata>> CASH = new ConcurrentHashMap<>();

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private StorageType storageType = DROP_BOX;
    private int maxCashSize = 300;
    private String accessToken;
    private String clientIdentifier;
    private DbxClientV2 client;

    @PostConstruct
    public void init() {
        DbxRequestConfig config = new DbxRequestConfig(clientIdentifier);
        client = new DbxClientV2(config, accessToken);
    }

    @Override
    public byte[] downloadFile(String fileName) {
        try {
            DbxDownloader<FileMetadata> metaInfo = findMeta(fileName);
            return metaInfo.getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("Can't download file from {} storage, reason: {}", storageType, e);
            return new byte[0];
        }
    }

    @Override
    public byte[] downloadFile(String fileName, String[] ranges) {
        try {
            int offset = toInt(ranges[0]);
            int limit = toInt(ranges[1]);
            byte[] bytes = new byte[limit - offset + 1];

            DbxDownloader<FileMetadata> metaInfo = findMeta(fileName);
            metaInfo.getInputStream().readNBytes(bytes, offset, limit);
            return bytes;
        } catch (Exception e) {
            log.error("Can't download file from {} storage, reason: {}", storageType, e);
            return new byte[0];
        }
    }

    private DbxDownloader<FileMetadata> findMeta(String fileName) throws DbxException {
        DbxDownloader<FileMetadata> metaInfo = CASH.get(fileName);
        if (metaInfo == null) {
            if (CASH.size() > maxCashSize) {
                service.execute(this::clearCash);
            }
            metaInfo = client.files().download("/" + fileName);
            CASH.put(fileName, metaInfo);
        }
        return metaInfo;
    }

    private void clearCash() {
        CASH.forEach((k, v) -> v.close());
        CASH.clear();
    }

    public boolean uploadFile(String file) {
        try (InputStream in = new FileInputStream(file)) {
            client.files()
                    .uploadBuilder("/" + VideoHelper.getNameFromPath(file))
                    .uploadAndFinish(in);
            return true;
        } catch (Exception e) {
            log.error("Can't upload file to {} storage, reason: {}", storageType, e.getMessage());
            return false;
        }
    }
}
