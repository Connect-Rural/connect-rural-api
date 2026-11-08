package com.crdev.connect_rural_api.business.file;

import java.io.InputStream;

public interface FileGateway {
    String getBucket();
    void upload(String objectKey, InputStream inputStream, long size, String contentType);
    InputStream download(String objectKey);
    void delete(String objectKey);
}
