package org.WoodiesGit.UnitTests;

import org.WoodiesGit.Blob;
import org.WoodiesGit.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

public class BlobTest {

    private File tempFile;

    @BeforeEach
    void setup() throws IOException {
        File gitDir = new File(".git");
        File objectsDir = new File(".git/objects");
        if (!objectsDir.exists()) {
            objectsDir.mkdirs();
        }

        tempFile = File.createTempFile("test", ".txt");
        Files.write(tempFile.toPath(), "hello world".getBytes());
    }

    @AfterEach
    void cleanup() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testBuildBlobCreatesCorrectFormat() throws IOException {
        byte[] content = "hello world".getBytes();

        byte[] blob = Blob.buildBlob(content);

        String blobStr = new String(blob);
        assertTrue(blobStr.startsWith("blob 11\0"));
        assertEquals("blob 11\0hello world", blobStr);
    }

    @Test
    void testBuildBlobEmptyContent() throws IOException {
        byte[] content = new byte[0];

        byte[] blob = Blob.buildBlob(content);

        assertEquals("blob 0\0", new String(blob));
    }

    @Test
    void testBuildBlobLargeContent() throws IOException {
        byte[] content = new byte[1000];

        byte[] blob = Blob.buildBlob(content);

        assertTrue(new String(blob).startsWith("blob 1000\0"));
        assertEquals(1000 + "blob 1000\0".length(), blob.length);
    }

    @Test
    void testBuildBlobFileThrowsForNonExistentFile() {
        assertThrows(RuntimeException.class, () -> {
            Blob.buildBlobFile("non-existent-file.txt");
        });
    }

    @Test
    void testBuildBlobFileSameContentSameHash() throws IOException {
        // Debug: Check if .git/objects exists
        File objectsDir = new File(".git/objects");
        System.out.println(".git/objects exists: " + objectsDir.exists());
        System.out.println("Current dir: " + System.getProperty("user.dir"));

        File tempFile2 = File.createTempFile("test2", ".txt");
        Files.write(tempFile2.toPath(), "hello world".getBytes());

        // Debug: Check if temp files exist
        System.out.println("tempFile exists: " + tempFile.exists());
        System.out.println("tempFile path: " + tempFile.getPath());
        System.out.println("tempFile2 exists: " + tempFile2.exists());

        File result1 = Blob.buildBlobFile(tempFile.getPath());
        File result2 = Blob.buildBlobFile(tempFile2.getPath());

        // Debug: Check results
        System.out.println("result1: " + result1);
        System.out.println("result2: " + result2);

        assertNotNull(result1, "result1 should not be null");
        assertNotNull(result2, "result2 should not be null");
        assertEquals(result1.getPath(), result2.getPath());

        tempFile2.delete();
    }

    @Test
    void testBuildBlobFileCompressesContent() throws IOException {
        File result = Blob.buildBlobFile(tempFile.getPath());
        byte[] compressed = Files.readAllBytes(result.toPath());

        assertTrue(compressed.length > 0);
        assertEquals(0x78, compressed[0] & 0xFF);

        byte[] decompressed = Util.decompress(compressed);
        assertEquals("blob 11\0hello world", new String(decompressed));
    }
}