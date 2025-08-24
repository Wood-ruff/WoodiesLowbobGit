package org.WoodiesGit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.IIOException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Index {

    public byte[] indexFileContent;
    public int fileAmountCurrent;
    public List<String> currentFiles;
    public byte[] currentEntries;
    public byte[] currentHeader;
    public byte[] currentExtensions;


    public Index() {
        try {
            this.indexFileContent = readIndexFile();
            if (this.indexFileContent.length == 0) {
                this.fileAmountCurrent = 0;
                this.currentFiles = new ArrayList<>();
                this.currentEntries = new byte[0];
                this.currentExtensions = new byte[0];
                this.currentHeader = new byte[0];
            } else {

                this.fileAmountCurrent = extractFileAmountFromIndex(this.indexFileContent);
                this.initExtractDataBlocksFromIndex(this.indexFileContent);
            }
        } catch (IOException e) {
            System.out.println("could not find index");
        }
    }

    public boolean updateIndex(String[] paths) {
        try {
            byte[] indexContent = buildIndex(Arrays.asList(paths));
            File indexFile = new File(findRepoRoot() + File.separator + ".git" + File.separator + "index");
            Files.write(indexFile.toPath(), indexContent);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Could not build Indext");
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public byte[] buildIndex(List<String> pathsRaw) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream indexOutStr = new ByteArrayOutputStream();
        List<String> paths = (List<String>) CollectionUtils.removeAll(pathsRaw, this.currentFiles);


        addIndexHeader(indexOutStr, this.fileAmountCurrent + paths.size());
        if (this.indexFileContent.length > 12) {
            indexOutStr.write(this.currentEntries);
        }

        for (String path : paths) {
            ByteArrayOutputStream entry = new ByteArrayOutputStream();
            File indexedFile = new File(path);
            addTimeStamps(entry, indexedFile);
            addMisc(entry, indexedFile);
            addFileMeta(entry, indexedFile);
            padArray(entry);
            indexOutStr.write(entry.toByteArray());
        }

        indexOutStr.write(this.currentExtensions);
        byte[] checksum = Util.hashBytes(indexOutStr.toByteArray());
        //checksum 20 last byte of array 20bytes
        indexOutStr.write(checksum);

        return indexOutStr.toByteArray();
    }

    public void initExtractDataBlocksFromIndex(byte[] indexFileContent) {
        this.currentHeader = Arrays.copyOfRange(indexFileContent, 0, 12);
        this.currentFiles = this.findAllCurrentFiles(this.indexFileContent, this.fileAmountCurrent);

    }

    public byte[] readIndexFile() throws IOException {
        File indexFile = new File(findRepoRoot() + File.separator + ".git" + File.separator + "index");

        if (!indexFile.exists()) {
            return new byte[0];
        }
        return Files.readAllBytes(indexFile.toPath());
    }

    public int extractFileAmountFromIndex(byte[] index) {
        if (index.length == 0) {
            return 0;
        }
        byte[] fileAmountBytes = {index[8], index[9], index[10], index[11]};
        int entryCount = ((fileAmountBytes[0] & 0xFF) << 24) |
                ((fileAmountBytes[1] & 0xFF) << 16) |
                ((fileAmountBytes[2] & 0xFF) << 8) |
                (fileAmountBytes[3] & 0xFF);
        return entryCount;
    }

    public List<String> findAllCurrentFiles(byte[] currentIndex, int entryCount) {
        List<String> paths = new ArrayList<>();
        if (currentIndex.length == 0) return paths;

        //exclude header, noting byte not arraypos for ease of calculation
        int currentByte = 12;
        for (int i = 0; i < entryCount; i++) {
            currentByte += 61;
            byte flagByte1 = currentIndex[currentByte - 1];
            byte flagByte2 = currentIndex[currentByte];
            currentByte += 2;
            int flags = ((flagByte1 & 0xFF) << 8) | (flagByte2 & 0xFF);
            // Extract only the lower 12 bits using a mask, as other 4 bits make up merge flags
            int nameLength = flags & 0x0FFF;
            byte[] name = Arrays.copyOfRange(currentIndex, currentByte - 1, currentByte + nameLength-1);
            paths.add(new String(name, StandardCharsets.UTF_8));
            currentByte += nameLength;
            currentByte = Util.nextDivisibleNumber(currentByte - 12, 8) + 12;
        }
        this.currentEntries = Arrays.copyOfRange(currentIndex, 12, currentByte);
        this.currentExtensions = Arrays.copyOfRange(currentIndex, currentByte, currentIndex.length - 20);
        return paths;
    }

    public void addIndexHeader(ByteArrayOutputStream index, int filesAmount) {

        //Signature byte 1-4 pos0-3 4bytes
        index.write('D');
        index.write('I');
        index.write('R');
        index.write('C');

        //version byte 5-8 pos 4-7 4bytes
        writeInt(index, 2);
        //amount of files byte 9-12 pos 8-11 4bytes
        writeInt(index, filesAmount);
    }

    public void addTimeStamps(ByteArrayOutputStream index, File indexedFile) {
        int timestampAddSeconds = (int) (System.currentTimeMillis() / 1000);
        int timestampAddLastMod = (int) (indexedFile.lastModified() / 1000);

        //commit time seconds, byte 1-4 pos 0-3 4bytes
        writeInt(index, timestampAddSeconds);
        //commit time nanosec, byte 5-8 pos 4-7 4bytes
        writeInt(index, 0);
        //last mod time seconds, byte 9-12 pos 8-11 4bytes
        writeInt(index, timestampAddLastMod);
        //last mod time nanosec, byte 13-16 pos 12-15 4bytes
        writeInt(index, 0);
    }

    public void addMisc(ByteArrayOutputStream index, File indexedFile) {
        //device id, byte 17-20 pos 16-19 4bytes
        writeInt(index, 0);
        //inode number, byte 21-24 pos 20-23 4bytes
        writeInt(index, 0);
        //filemode/permissions byte 25-28,pos 24-27 4bytes
        writeInt(index, 33188);
        //user id, byte 29-32 pos 28-31 4bytes
        writeInt(index, 0);
        //group id, byte 33-36 pos 32-35 4bytes
        writeInt(index, 0);
    }

    private void addFileMeta(ByteArrayOutputStream indexOutStr, File indexedFile) throws RuntimeException, IOException {
        //filesize, byte 37-40 pos 36-39 4bytes
        writeInt(indexOutStr, (int) indexedFile.length());
        byte[] sha1 = Blob.buildHash(indexedFile.getPath());
        //content hashed, byte 41-60 pos 40-59 20bytes
        indexOutStr.write(sha1);
        String relativePath = calculateRelativePath(indexedFile, findRepoRoot());
        //flags, byte 61-62 pos 60-61 bits 16-13 stage(merge conflicts) bits 12-1 name length   2bytes
        writeShort(indexOutStr, (short) relativePath.length());
        //filename (as path) variable length byte 63+ pos 62+ variable bytes
        indexOutStr.write(relativePath.getBytes());
    }

    public void writeVariable(ByteArrayOutputStream out, int value, int byteSize) {
        for (int i = byteSize - 1; i >= 0; i--) {
            out.write((value >> 8 * i) & 0xFF);
        }
    }

    public void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    public void writeShort(ByteArrayOutputStream out, short value) {
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }


    public void padArray(ByteArrayOutputStream out) {
        while (out.size() % 8 != 0) {
            out.write(0x00);
        }
    }


    public String calculateRelativePath(File indexedFile, String repoPath) {
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

    public String findRepoRoot() {
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
