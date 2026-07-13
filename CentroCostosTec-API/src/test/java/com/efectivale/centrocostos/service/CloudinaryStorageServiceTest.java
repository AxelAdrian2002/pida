package com.efectivale.centrocostos.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class CloudinaryStorageServiceTest {

    @Test
    void shouldFailWhenFileIsNull() {
        CloudinaryStorageService service = new CloudinaryStorageService("cloud", "key", "secret", "folder");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.uploadImage(null, "perfil")
        );

        assertTrue(ex.getMessage().contains("Debes adjuntar una imagen"));
    }

    @Test
    void shouldFailWhenFileIsNotImage() {
        CloudinaryStorageService service = new CloudinaryStorageService("cloud", "key", "secret", "folder");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "doc.txt",
            "text/plain",
            "hola".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.uploadImage(file, "perfil")
        );

        assertTrue(ex.getMessage().contains("Solo se permiten archivos de imagen"));
    }

    @Test
    void shouldFailWhenCloudinaryConfigIsMissing() {
        CloudinaryStorageService service = new CloudinaryStorageService("", "", "", "folder");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            "image/png",
            new byte[] {1, 2, 3}
        );

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.uploadImage(file, "perfil")
        );

        assertTrue(ex.getMessage().contains("Cloudinary no está configurado"));
    }
}
