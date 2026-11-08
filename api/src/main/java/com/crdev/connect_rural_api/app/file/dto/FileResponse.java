package com.crdev.connect_rural_api.app.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String key;
    private String originalName;
    private String contentType;
    private Long size;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
