package com.khudim.storage;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.khudim.storage.StorageType.GOOGLE_DRIVE;

@Data
@Component
@ConditionalOnProperty(prefix = "google-drive", value = "accessToken")
public class GoogleDriveStorage implements IFileStorage {

    private StorageType storageType = GOOGLE_DRIVE;

    @Value("google-drive.accessToken")
    private String accessToken;

    @Value("google-drive.clientIdentifier")
    private String clientIdentifier;

    public GoogleDriveStorage() {
   /*     GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        Plus plus = new Plus.builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Google-PlusSample/1.0")
                .build();
   */
    }

    @Override
    public byte[] downloadFile(String fileName) {
        return new byte[0];
    }

    @Override
    public byte[] downloadFile(String fileName, String[] ranges) {
        return new byte[0];
    }

    @Override
    public boolean uploadFile(String file) {
        return false;
    }
}
