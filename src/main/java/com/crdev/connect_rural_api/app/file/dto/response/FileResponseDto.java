package com.crdev.connect_rural_api.app.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    private String key;
    private String originalName;
    private String contentType;
    private Long size;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
