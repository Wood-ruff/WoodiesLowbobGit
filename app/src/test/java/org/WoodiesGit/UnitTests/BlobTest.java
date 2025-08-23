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
    void testHashBytesProducesCorrectSHA1() throws NoSuchAlgorithmException {
        byte[] input = "test content".getBytes();

        byte[] hash = Blob.hashBytes(input);

        assertEquals(20, hash.length);
        String hexHash = Util.bytesToHex(hash);
        assertEquals(40, hexHash.length());
    }

    @Test
    void testHashBytesDeterministic() throws NoSuchAlgorithmException {
        byte[] input = "same input".getBytes();

        byte[] hash1 = Blob.hashBytes(input);
        byte[] hash2 = Blob.hashBytes(input);

        assertArrayEquals(hash1, hash2);
    }

    @Test
    void testHashBytesDifferentInputDifferentHash() throws NoSuchAlgorithmException {
        byte[] input1 = "input one".getBytes();
        byte[] input2 = "input two".getBytes();

        byte[] hash1 = Blob.hashBytes(input1);
        byte[] hash2 = Blob.hashBytes(input2);

        assertFalse(Util.bytesToHex(hash1).equals(Util.bytesToHex(hash2)));
    }

    @Test
    void testBuildBlobFileCreatesFileInCorrectLocation() {
        File result = Blob.buildBlobFile(tempFile.getPath());

        assertTrue(result.exists());
        assertTrue(result.getPath().contains(".git" + File.separator + "objects"));

        String path = result.getPath();
        String[] parts = path.split(File.separator.equals("\\") ? "\\\\" : File.separator);
        String folderName = parts[parts.length - 2];
        String fileName = parts[parts.length - 1];

        assertEquals(2, folderName.length());
        assertEquals(38, fileName.length());
    }

    @Test
    void testBuildBlobFileThrowsForNonExistentFile() {
        assertThrows(RuntimeException.class, () -> {
            Blob.buildBlobFile("non-existent-file.txt");
        });
    }

    @Test
    void testBuildBlobFileSameContentSameHash() throws IOException {
        File tempFile2 = File.createTempFile("test2", ".txt");
        Files.write(tempFile2.toPath(), "hello world".getBytes());

        File result1 = Blob.buildBlobFile(tempFile.getPath());
        File result2 = Blob.buildBlobFile(tempFile2.getPath());

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