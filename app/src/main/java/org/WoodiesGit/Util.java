package org.WoodiesGit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Util {
    public static boolean deleteFile(File toDelete){
        boolean success = false;
        if(toDelete.exists()){
            return toDelete.delete();
        }
        return success;
    }

    public static boolean cleanupAll(){
        List<String> pathsToClean = Arrays.asList(".git/objects/",".git/HEAD",".git/refs/heads/",".git/refs/",".git/");
        boolean success = false;

        for(String i:pathsToClean){
            File f = new File(i);
            deleteFile(f);
        }

        return success;
    }

    public static byte[] readFilesByteContent(String path) throws IOException {
        File file = new File(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        return fileContent;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static byte[] compress(byte[] content) throws IOException{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflateOutstream = new DeflaterOutputStream(outStream);
        deflateOutstream.write(content);
        deflateOutstream.finish();
        deflateOutstream.close();
        return outStream.toByteArray();
    }

    public static byte[] decompress(byte[] compressed) throws IOException {
        InflaterInputStream inflater = new InflaterInputStream(
                new ByteArrayInputStream(compressed)
        );

        return inflater.readAllBytes();
    }


    public static String normalizePath(String path) {
        // Check if path already starts with relative or absolute path indicators
        if (path.startsWith("./") || path.startsWith("../") || path.startsWith("/")) {
            return path;
        }

        // Check for Windows absolute paths (C:\ or C:/ style)
        if (path.length() >= 3 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':' &&
                (path.charAt(2) == '\\' || path.charAt(2) == '/')) {
            return path;
        }

        // Add ./ prefix for simple filenames
        return "."+File.separator + path;
    }
}
