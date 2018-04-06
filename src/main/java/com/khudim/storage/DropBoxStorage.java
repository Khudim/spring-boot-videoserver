package com.khudim.storage;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.khudim.utils.VideoHelper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.khudim.storage.StorageType.DROP_BOX;

@Component
@Data
@ConditionalOnProperty(prefix = "storage.drop-box", value = "accessToken")
@ConfigurationProperties(prefix = "storage.drop-box")
public class DropBoxStorage implements IFileStorage {

    private final static Logger log = LoggerFactory.getLogger(DropBoxStorage.class);

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
            DbxDownloader<FileMetadata> metaInfo = client.files().download("/" + VideoHelper.getNameFromPath(fileName));
            return metaInfo.getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("Can't download file from {} storage, reason: {}", storageType, e);
            return new byte[0];
        }
    }

    @Override
    public byte[] downloadFile(String fileName, int offset, int limit) throws NoSuchFileException {
        try (DbxDownloader<FileMetadata> metaInfo = client.files().download("/" + VideoHelper.getNameFromPath(fileName))) {
            byte[] bytes = new byte[limit - offset];
            InputStream inputStream = metaInfo.getInputStream();
            inputStream.skip(offset);
            inputStream.readNBytes(bytes, 0, limit - offset);
            return bytes;
        } catch (Exception e) {
            throw new NoSuchFileException("Can't download file, reason: " + e);
        }
    }

    public boolean uploadFile(String file) {
        log.debug("Start upload file to {} storage.", storageType);
        try (InputStream in = new FileInputStream(file)) {
            client.files()
                    .uploadBuilder("/" + VideoHelper.getNameFromPath(file))
                    .uploadAndFinish(in);
            return true;
        } catch (Exception e) {
            log.error("Can't upload file to {} storage, reason: {}", storageType, e.getCause());
            return false;
        }
    }
}
