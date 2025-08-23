package org.WoodiesGit;

import javax.imageio.IIOException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Index {


    public static byte[] buildIndex(String path,byte[] hash) throws IOException {
        File indexedFile = new File(path);
        ByteArrayOutputStream indexOutStr = new ByteArrayOutputStream();
        addIndexHeader(indexOutStr);
        addTimeStamps(indexOutStr,indexedFile);
        addFileMeta(indexOutStr,indexedFile);

        return indexOutStr.toByteArray();
    }


    public static void addIndexHeader(ByteArrayOutputStream index){
        index.write('D');
        index.write('I');
        index.write('R');
        index.write('C');
        writeInt(index,2);
        writeInt(index,1);
    }

    public static void addTimeStamps(ByteArrayOutputStream index,File indexedFile){
        int timestampAddSeconds = (int) (System.currentTimeMillis() /1000);
        int timestampAddLastMod = (int) (indexedFile.lastModified() /1000);

        writeInt(index,timestampAddSeconds);
        writeInt(index,0);
        writeInt(index,timestampAddLastMod);
        writeInt(index,0);
    }

    public static void addMisc(ByteArrayOutputStream index, File indexedFile){
        writeInt(index,0);
        writeInt(index,0);
        writeInt(index,33188);
        writeInt(index,0);
        writeInt(index,0);
    }

    private static void addFileMeta(ByteArrayOutputStream indexOutStr, File indexedFile) throws RuntimeException, IOException {
        writeInt(indexOutStr,(int) indexedFile.length());
        byte[] sha1 = Blob.buildHash(indexedFile.getPath());
        indexOutStr.write(sha1);
        String relativePath = calculateRelativePath(indexedFile,findRepoRoot());
        writeShort(indexOutStr,(short) relativePath.length());
        indexOutStr.write(relativePath.getBytes());
    }

    public static void writeVariable(ByteArrayOutputStream out, int value,int byteSize){
        for(int i = byteSize-1; i >= 0; i--){
            out.write((value >> 8*i) & 0xFF);
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


    public static void padArray(ByteArrayOutputStream out){
        while(out.size()%8 !=0){
            out.write(0x00);
        }
    }


    public static String calculateRelativePath(File indexedFile,String repoPath){
        String filePath = indexedFile.getAbsolutePath();
        String relativePath;
        if (filePath.startsWith(repoPath)) {
            relativePath = filePath.replace(repoPath,"");
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
