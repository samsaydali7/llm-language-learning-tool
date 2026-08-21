package com.languagelearning.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void truncatesAVeryLongFilenameSoTheStoredNameStaysWithinFilesystemLimits() {
        StorageService storage = new StorageService(new StorageProperties(tempDir.toString()));

        // A real downloaded ebook filename, well over most filesystems' 255-byte-per-component cap
        // once the 36-char UUID prefix store() adds is accounted for.
        String longName = "French All-in-One For Dummies [eBook - NC Digital Library -- Eliane Kurbegov, "
                + "Dodi-Katrin Schmidt, Michelle M Williams -- For Dummies, 1, 2012 -- isbn13 9781118228159 "
                + "-- 73bfd541c5f544e6f2c0395c2a12dae8 -- Archive.pdf";
        assertThat(longName.length()).isGreaterThan(200);

        String relativePath = storage.storeBytes("fr", 1L, "pdf", longName, new ByteArrayInputStream("hi".getBytes()));

        String storedFilename = Path.of(relativePath).getFileName().toString();
        assertThat(storedFilename.length()).isLessThanOrEqualTo(200);
        assertThat(storedFilename).endsWith(".pdf");
        // The stored file must actually exist on disk under that (now safe) name.
        assertThat(storage.resolve(relativePath)).exists();
    }

    @Test
    void keepsAShortFilenameUnchangedApartFromSanitization() {
        StorageService storage = new StorageService(new StorageProperties(tempDir.toString()));

        String relativePath = storage.storeBytes("fr", 1L, "audio", "track 01.mp3", new ByteArrayInputStream("hi".getBytes()));

        String storedFilename = Path.of(relativePath).getFileName().toString();
        assertThat(storedFilename).endsWith("-track_01.mp3");
    }
}
