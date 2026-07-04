package com.efectivale.centrocostos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryStorageService {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String baseFolder;

    public CloudinaryStorageService(
        @Value("${app.cloudinary.cloud-name:}") String cloudName,
        @Value("${app.cloudinary.api-key:}") String apiKey,
        @Value("${app.cloudinary.api-secret:}") String apiSecret,
        @Value("${app.cloudinary.folder:centrocostos-tec}") String baseFolder
    ) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseFolder = baseFolder;
    }

    public Map<String, String> uploadImage(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar una imagen");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        if (cloudName == null || cloudName.isBlank() || apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException("Cloudinary no está configurado. Define APP_CLOUDINARY_CLOUD_NAME, APP_CLOUDINARY_API_KEY y APP_CLOUDINARY_API_SECRET");
        }

        String folder = (baseFolder == null || baseFolder.isBlank()) ? "centrocostos-tec" : baseFolder.trim();
        if (subFolder != null && !subFolder.isBlank()) {
            folder = folder + "/" + subFolder.trim();
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);

        try {
            Map<String, Object> result = subirConCloudinaryReflexion(file.getBytes(), config, folder);

            String secureUrl = String.valueOf(result.getOrDefault("secure_url", ""));
            String publicId = String.valueOf(result.getOrDefault("public_id", ""));

            if (secureUrl.isBlank()) {
                throw new IllegalStateException("Cloudinary no devolvió una URL válida");
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", secureUrl);
            response.put("publicId", publicId);
            return response;
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible subir la imagen a Cloudinary", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> subirConCloudinaryReflexion(byte[] bytes, Map<String, String> config, String folder) {
        try {
            Class<?> cloudinaryClass = Class.forName("com.cloudinary.Cloudinary");
            Object cloudinary = cloudinaryClass.getConstructor(Map.class).newInstance(config);

            Object uploader = cloudinaryClass.getMethod("uploader").invoke(cloudinary);

            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", folder);
            uploadOptions.put("resource_type", "image");

            Object raw = uploader.getClass()
                .getMethod("upload", Object.class, Map.class)
                .invoke(uploader, bytes, uploadOptions);

            if (raw instanceof Map<?, ?> mapResult) {
                return (Map<String, Object>) mapResult;
            }

            throw new IllegalStateException("Respuesta inválida de Cloudinary");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Libreria de Cloudinary no disponible en classpath", ex);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("No fue posible inicializar el cliente de Cloudinary", ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            throw new IllegalStateException("Cloudinary devolvio un error al subir imagen", cause);
        }
    }
}
