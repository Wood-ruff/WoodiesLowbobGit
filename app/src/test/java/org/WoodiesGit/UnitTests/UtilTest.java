package org.WoodiesGit.UnitTests;

import org.WoodiesGit.Util;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.zip.InflaterInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {
    @Test
    void testBytesToHexConvertsCorrect() {
        byte[] input = new byte[]{
                (byte) 0xAB,  // 171 in decimal
                (byte) 0xCD,  // 205 in decimal
                (byte) 0x12,  // 18 in decimal
                (byte) 0x00   // 0 in decimal
        };
        String result = Util.bytesToHex(input);
        assertEquals("abcd1200", result);
    }

    @Test
    void testBytesToHexEmptyBytes() {
        byte[] input = new byte[0];
        String result = Util.bytesToHex(input);
        assertEquals("", result);
    }

    @Test
    void testBytesToHexAllZero() {
        byte[] input = new byte[]{
                0x00,
                0x00,
                0x00,
                0x00,
        };
        String result = Util.bytesToHex(input);
        assertEquals("00000000", result);
    }

    @Test
    void testBytesToHexPadsSmallValues() {
        byte[] input = new byte[]{
                (byte) 0x00,  // Should become "00" not "0"
                (byte) 0x01,  // Should become "01" not "1"
                (byte) 0x0F,  // Should become "0f" not "f"
                (byte) 0x10   // Should become "10" (no padding needed)
        };

        String result = Util.bytesToHex(input);

        assertEquals("00010f10", result);
        assertEquals(8, result.length()); // 4 bytes = 8 hex chars
    }

    @Test
    void testBytesToHexSingleSmallByte() {
        byte[] input = new byte[]{(byte) 0x09};

        String result = Util.bytesToHex(input);

        assertEquals("09", result);  // NOT "9"
        assertEquals(2, result.length());
    }

    @Test
    void testCompressContentValid() {
        byte[] input = "Teststring".getBytes();
        try {
            byte[] compressed = Util.compress(input);
            InflaterInputStream inflater = new InflaterInputStream(
                    new ByteArrayInputStream(compressed)
            );

            assertEquals("Teststring", new String(inflater.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            fail("Compress threw unexpected exception: " + e.getMessage());
        }

    }

    @Test
    void testCompressSize() {
        byte[] input = "ReallyBigTestStringOMGItsSoBigWillThisEvenCompress?".getBytes();
        try {
            byte[] compressed = Util.compress(input);
            assertNotSame(input.length, compressed.length);
        } catch (IOException e) {
            fail("Compress threw unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testCompressEmpty() {
        byte[] input = "".getBytes();
        try {
            byte[] compressed = Util.compress(input);
            assertTrue(compressed.length > 0);

            assertEquals(0x78, compressed[0] & 0xFF);

            byte[] decompressed = Util.decompress(compressed);
            assertEquals(0, decompressed.length);
        } catch (IOException e) {
            fail("Compress threw unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testReadFilesByteContent() {
        String path = getClass().getClassLoader()
                .getResource("TestStringFile.txt")
                .getPath();
        try {
            byte[] content = Util.readFilesByteContent(path);

            assertEquals("This is a Testfile", new String(content));
        } catch (IOException e) {
            fail("readFilesByteContent threw unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testReadFilesByteEmpty() {
        String path = getClass().getClassLoader()
                .getResource("EmptyFile.txt")
                .getPath();
        try {
            byte[] content = Util.readFilesByteContent(path);

            assertEquals("", new String(content));
        } catch (IOException e) {
            fail("readFilesByteContent threw unexpected exception: " + e.getMessage());
        }
    }


    @Test
    void testReadFilesByteNoneExistent() {
        Throwable exception = assertThrows(NoSuchFileException.class,() ->{Util.readFilesByteContent("FileDoesntExist.txt");});
        assertEquals(NoSuchFileException.class,exception.getClass());
    }

    @Test
    void testDeleteExistingFile() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        assertTrue(tempFile.exists());

        boolean result = Util.deleteFile(tempFile);

        assertTrue(result);
        assertFalse(tempFile.exists());
    }

    @Test
    void testDeleteNonExistentFile() {
        File nonExistent = new File("this-file-does-not-exist-12345.txt");
        assertFalse(nonExistent.exists());

        boolean result = Util.deleteFile(nonExistent);

        assertFalse(result);
    }

    @Test
    void testDeleteDirectory() throws IOException {
        File tempDir = Files.createTempDirectory("testdir").toFile();
        assertTrue(tempDir.exists());
        assertTrue(tempDir.isDirectory());

        boolean result = Util.deleteFile(tempDir);

        assertTrue(result);
        assertFalse(tempDir.exists());
    }

    @Test
    void testDeleteDirectoryWithContents() throws IOException {
        File tempDir = Files.createTempDirectory("testdir").toFile();
        File fileInDir = new File(tempDir, "file.txt");
        Files.write(fileInDir.toPath(), "content".getBytes());

        boolean result = Util.deleteFile(tempDir);

        assertFalse(result);
        assertTrue(tempDir.exists());

        fileInDir.delete();
        tempDir.delete();
    }

}