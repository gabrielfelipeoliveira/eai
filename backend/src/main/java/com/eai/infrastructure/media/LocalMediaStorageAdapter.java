package com.eai.infrastructure.media;

import com.eai.application.common.ApplicationException;
import com.eai.application.media.MediaObject;
import com.eai.application.media.MediaStoragePort;
import com.eai.application.media.StoreMediaCommand;
import com.eai.application.media.StoredMedia;
import com.eai.infrastructure.config.MediaStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalMediaStorageAdapter implements MediaStoragePort {

    private static final String PROVIDER = "local";

    private final MediaStorageProperties properties;

    @Override
    public StoredMedia store(StoreMediaCommand command) {
        if (!PROVIDER.equals(properties.effectiveProvider())) {
            throw new ApplicationException("MEDIA_STORAGE_PROVIDER_UNSUPPORTED", "Configured media storage provider is not supported");
        }
        byte[] content = command.content();
        if (content == null || content.length == 0) {
            throw new ApplicationException("MEDIA_STORAGE_EMPTY_FILE", "Media file is empty");
        }
        String sha256 = command.sha256() == null || command.sha256().isBlank() ? sha256(content) : command.sha256().trim();
        String fileName = sanitizeFileName(command.fileName());
        String key = key(command, fileName);
        Path target = root().resolve(key).normalize();
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return new StoredMedia(PROVIDER, key, fileName, command.mimeType(), content.length, sha256);
        } catch (IOException exception) {
            throw new ApplicationException("MEDIA_STORAGE_WRITE_FAILED", "Could not store media file");
        }
    }

    @Override
    public MediaObject read(String provider, String key) {
        if (!PROVIDER.equals(provider) || key == null || key.isBlank()) {
            throw new ApplicationException("MEDIA_STORAGE_OBJECT_NOT_FOUND", "Media file not found");
        }
        Path target = root().resolve(key).normalize();
        if (!target.startsWith(root())) {
            throw new ApplicationException("MEDIA_STORAGE_OBJECT_NOT_FOUND", "Media file not found");
        }
        try {
            byte[] content = Files.readAllBytes(target);
            String fileName = target.getFileName().toString();
            return new MediaObject(PROVIDER, key, fileName, null, content.length, sha256(content), content);
        } catch (IOException exception) {
            throw new ApplicationException("MEDIA_STORAGE_OBJECT_NOT_FOUND", "Media file not found");
        }
    }

    private Path root() {
        return Path.of(properties.effectiveLocalDirectory()).toAbsolutePath().normalize();
    }

    private String key(StoreMediaCommand command, String fileName) {
        String source = sanitizePathPart(command.source() == null ? "media" : command.source());
        String externalId = sanitizePathPart(command.externalMediaId() == null ? UUID.randomUUID().toString() : command.externalMediaId());
        return command.companyId() + "/" + command.storeId() + "/" + source + "/" + externalId + "/" + fileName;
    }

    private String sanitizeFileName(String fileName) {
        String value = fileName == null || fileName.isBlank() ? "media.bin" : fileName.trim();
        value = value.replace('\\', '_').replace('/', '_').replaceAll("[^A-Za-z0-9._-]", "_");
        return value.length() > 120 ? value.substring(value.length() - 120) : value;
    }

    private String sanitizePathPart(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception exception) {
            throw new ApplicationException("MEDIA_STORAGE_HASH_FAILED", "Could not calculate media checksum");
        }
    }
}
