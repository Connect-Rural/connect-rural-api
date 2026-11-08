package com.crdev.connect_rural_api.business.file.dto;

import com.crdev.connect_rural_api.data.file.FileObjectEntity;

import java.io.InputStream;

public record FileDownloadPayload(FileObjectEntity file, InputStream stream) {}
