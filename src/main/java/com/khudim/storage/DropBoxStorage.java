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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.lang.math.NumberUtils.toInt;

@Component
@Data
public class DropBoxStorage implements IFileStorage {

    private final static Logger log = LoggerFactory.getLogger(HtmlParser.class);
    private final static Map<String, DbxDownloader<FileMetadata>> CASH = new ConcurrentHashMap<>();

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private String storageName = "DropBox";

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
    public byte[] downloadFile(String fileName, String[] ranges) {
        try {
            int offset = toInt(ranges[0]);
            int limit = toInt(ranges[1]);
            byte[] bytes = new byte[limit - offset + 1];

            DbxDownloader<FileMetadata> metaInfo = findMeta(fileName);
            metaInfo.getInputStream().readNBytes(bytes, offset, limit);
            return bytes;
        } catch (Exception e) {
            log.error("Can't download file from {} storage, reason: {}", storageName, e);
            return new byte[0];
        }
    }

    private DbxDownloader<FileMetadata> findMeta(String fileName) throws DbxException {
        DbxDownloader<FileMetadata> metaInfo = CASH.get(fileName);
        if (metaInfo == null) {
            if (CASH.size() > MAX_CASH_SIZE) {
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
            log.error("Can't upload file to {} storage, reason: {}", storageName, e.getMessage());
            return false;
        }
    }
}
