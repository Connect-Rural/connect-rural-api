package com.crdev.connect_rural_api.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    /**
     * Obtiene el bucket agregado como variable de entorno
     */
    String getBucket();
    /**
     * Sube un archivo y devuelve la clave (key) generada.
     */
    String upload(MultipartFile file, String key) throws IOException;

    /**
     * Descarga un archivo como un stream de bytes.
     */
    InputStream download(String key);

    /**
     * Elimina un archivo.
     */
    void delete(String key);

    /**
     * Obtiene una URL para acceder al archivo.
     * Puede ser una URL directa (local) o una URL prefirmada (S3).
     */
    String getUrl(String key);
}