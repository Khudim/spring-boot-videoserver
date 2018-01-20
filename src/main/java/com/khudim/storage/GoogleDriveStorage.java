package com.khudim.storage;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class GoogleDriveStorage implements IFileStorage {

    private String storageName = "GoogleDrive";

    @Value("dropBox.token")
    private String accessToken;

    @Value("dropBox.identifier")
    private String clientIdentifier;

    public GoogleDriveStorage() {
   /*     GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        Plus plus = new Plus.builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Google-PlusSample/1.0")
                .build();
   */
    }

    @Override
    public byte[] downloadFile(String fileName, String[] ranges) throws Exception {
        return new byte[0];
    }
}
