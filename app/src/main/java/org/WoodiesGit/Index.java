package org.WoodiesGit;

import javax.imageio.IIOException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Index {


    public static byte[] buildIndex(String[] paths, byte[] hash) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream indexOutStr = new ByteArrayOutputStream();
        byte[] indexFileContent = readIndexFile();
        int fileAmountCurrent = extractFileAmountFromIndex(indexFileContent);
        addIndexHeader(indexOutStr, fileAmountCurrent+paths.length);
        if(indexFileContent.length >12){
            byte[] entries = Arrays.copyOfRange(indexFileContent, 12, indexFileContent.length - 20);
            indexOutStr.write(entries);
        }

        for (String path : paths) {
            File indexedFile = new File(path);
            addTimeStamps(indexOutStr, indexedFile);
            addMisc(indexOutStr, indexedFile);
            addFileMeta(indexOutStr, indexedFile);
            padArray(indexOutStr);
        }

        byte[] checksum = Blob.hashBytes(indexOutStr.toByteArray());
        indexOutStr.write(checksum);

        return indexOutStr.toByteArray();
    }

    public static byte[] readIndexFile() throws IOException {
        File indexFile = new File(findRepoRoot() + "/.git/index");

        if(!indexFile.exists()){
            return new byte[0];
        }
        return Files.readAllBytes(indexFile.toPath());
    }

    public static int extractFileAmountFromIndex(byte[] index) {
        if(index.length == 0){
            return 0;
        }
        byte[] fileAmountBytes = {index[8], index[9], index[10], index[11]};
        int entryCount = ((fileAmountBytes[0] & 0xFF) << 24) |
                ((fileAmountBytes[1] & 0xFF) << 16) |
                ((fileAmountBytes[2] & 0xFF) << 8) |
                (fileAmountBytes[3] & 0xFF);
        return entryCount;
    }


    public static void addIndexHeader(ByteArrayOutputStream index, int filesAmount) {

        index.write('D');
        index.write('I');
        index.write('R');
        index.write('C');
        writeInt(index,2);
        writeInt(index,filesAmount);
    }

    public static void addTimeStamps(ByteArrayOutputStream index, File indexedFile) {
        int timestampAddSeconds = (int) (System.currentTimeMillis() / 1000);
        int timestampAddLastMod = (int) (indexedFile.lastModified() / 1000);

        writeInt(index, timestampAddSeconds);
        writeInt(index, 0);
        writeInt(index, timestampAddLastMod);
        writeInt(index, 0);
    }

    public static void addMisc(ByteArrayOutputStream index, File indexedFile) {
        writeInt(index, 0);
        writeInt(index, 0);
        writeInt(index, 33188);
        writeInt(index, 0);
        writeInt(index, 0);
    }

    private static void addFileMeta(ByteArrayOutputStream indexOutStr, File indexedFile) throws RuntimeException, IOException {
        writeInt(indexOutStr, (int) indexedFile.length());
        byte[] sha1 = Blob.buildHash(indexedFile.getPath());
        indexOutStr.write(sha1);
        String relativePath = calculateRelativePath(indexedFile, findRepoRoot());
        writeShort(indexOutStr, (short) relativePath.length());
        indexOutStr.write(relativePath.getBytes());
    }

    public static void writeVariable(ByteArrayOutputStream out, int value, int byteSize) {
        for (int i = byteSize - 1; i >= 0; i--) {
            out.write((value >> 8 * i) & 0xFF);
        }
    }

    public static void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    public static void writeShort(ByteArrayOutputStream out, short value) {
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }


    public static void padArray(ByteArrayOutputStream out) {
        while (out.size() % 8 != 0) {
            out.write(0x00);
        }
    }


    public static String calculateRelativePath(File indexedFile, String repoPath) {
        String filePath = indexedFile.getAbsolutePath();
        String relativePath;
        if (filePath.startsWith(repoPath)) {
            relativePath = filePath.substring(repoPath.length() + 1);
            relativePath = relativePath.replace(File.separatorChar, '/');
        } else {
            relativePath = indexedFile.getName();
        }

        return relativePath;
    }

    public static String findRepoRoot() {
        File current = new File(System.getProperty("user.dir"));

        while (current != null) {
            File gitDir = new File(current, ".git");
            if (gitDir.exists() && gitDir.isDirectory()) {
                return current.getAbsolutePath();
            }
            current = current.getParentFile();
        }
        return null;
    }
}
