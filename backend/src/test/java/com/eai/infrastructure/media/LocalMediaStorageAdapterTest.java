package com.eai.infrastructure.media;

import com.eai.application.common.ApplicationException;
import com.eai.application.media.MediaObject;
import com.eai.application.media.StoreMediaCommand;
import com.eai.application.media.StoredMedia;
import com.eai.infrastructure.config.MediaStorageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalMediaStorageAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    @TempDir
    Path storageDirectory;

    @DisplayName("Armazena midia local com nome e caminho sanitizados")
    @Test
    void storesLocalMediaWithSanitizedNameAndPath() throws Exception {
        LocalMediaStorageAdapter adapter = adapter("local");
        byte[] content = "conteudo".getBytes();

        StoredMedia storedMedia = adapter.store(new StoreMediaCommand(
                COMPANY_ID,
                STORE_ID,
                "whatsapp inbound",
                "media/123",
                "../nota fiscal?.jpg",
                "image/jpeg",
                content,
                null
        ));

        assertThat(storedMedia.provider()).isEqualTo("local");
        assertThat(storedMedia.key()).isEqualTo(COMPANY_ID + "/" + STORE_ID + "/whatsapp_inbound/media_123/.._nota_fiscal_.jpg");
        assertThat(storedMedia.fileName()).isEqualTo(".._nota_fiscal_.jpg");
        assertThat(storedMedia.mimeType()).isEqualTo("image/jpeg");
        assertThat(storedMedia.sizeBytes()).isEqualTo(content.length);
        assertThat(storedMedia.sha256()).isEqualTo(sha256(content));
        assertThat(Files.readAllBytes(storageDirectory.resolve(storedMedia.key()))).isEqualTo(content);
    }

    @DisplayName("Le midia local armazenada")
    @Test
    void readsStoredLocalMedia() throws Exception {
        LocalMediaStorageAdapter adapter = adapter(null);
        byte[] content = "arquivo".getBytes();
        StoredMedia storedMedia = adapter.store(new StoreMediaCommand(
                COMPANY_ID,
                STORE_ID,
                null,
                "abc",
                null,
                "application/octet-stream",
                content,
                " custom-sha "
        ));

        MediaObject mediaObject = adapter.read("local", storedMedia.key());

        assertThat(mediaObject.provider()).isEqualTo("local");
        assertThat(mediaObject.key()).isEqualTo(storedMedia.key());
        assertThat(mediaObject.fileName()).isEqualTo("media.bin");
        assertThat(mediaObject.sizeBytes()).isEqualTo(content.length);
        assertThat(mediaObject.sha256()).isEqualTo(sha256(content));
        assertThat(mediaObject.content()).isEqualTo(content);
    }

    @DisplayName("Rejeita provider local desabilitado")
    @Test
    void rejectsUnsupportedProvider() {
        LocalMediaStorageAdapter adapter = adapter("s3");

        assertThatThrownBy(() -> adapter.store(command("arquivo".getBytes())))
                .isInstanceOf(ApplicationException.class)
                .extracting("code")
                .isEqualTo("MEDIA_STORAGE_PROVIDER_UNSUPPORTED");
    }

    @DisplayName("Rejeita arquivo vazio")
    @Test
    void rejectsEmptyFile() {
        LocalMediaStorageAdapter adapter = adapter("local");

        assertThatThrownBy(() -> adapter.store(command(new byte[0])))
                .isInstanceOf(ApplicationException.class)
                .extracting("code")
                .isEqualTo("MEDIA_STORAGE_EMPTY_FILE");
    }

    @DisplayName("Bloqueia leitura fora do diretorio local")
    @Test
    void blocksPathTraversalRead() {
        LocalMediaStorageAdapter adapter = adapter("local");

        assertThatThrownBy(() -> adapter.read("local", "../secret.txt"))
                .isInstanceOf(ApplicationException.class)
                .extracting("code")
                .isEqualTo("MEDIA_STORAGE_OBJECT_NOT_FOUND");
    }

    @DisplayName("Retorna erro quando objeto local nao existe")
    @Test
    void returnsNotFoundWhenLocalObjectDoesNotExist() {
        LocalMediaStorageAdapter adapter = adapter("local");

        assertThatThrownBy(() -> adapter.read("local", "missing/file.jpg"))
                .isInstanceOf(ApplicationException.class)
                .extracting("code")
                .isEqualTo("MEDIA_STORAGE_OBJECT_NOT_FOUND");
    }

    private LocalMediaStorageAdapter adapter(String provider) {
        return new LocalMediaStorageAdapter(new MediaStorageProperties(provider, storageDirectory.toString()));
    }

    private StoreMediaCommand command(byte[] content) {
        return new StoreMediaCommand(COMPANY_ID, STORE_ID, "whatsapp", "media-id", "media.jpg", "image/jpeg", content, null);
    }

    private String sha256(byte[] content) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    }
}
