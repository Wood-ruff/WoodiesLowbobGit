package org.WoodiesGit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Util {


    /**
     * Deletes a File safely
     * @param toDelete File that needs to be deleted
     * @return Success state of the file deletion
     */
    public static boolean deleteFile(File toDelete){
        boolean success = false;
        if(toDelete.exists()){
            return toDelete.delete();
        }
        return success;
    }

    /**
     * Cleans up the entirety of files created by this project
     * @return Success state of cleanup
     */
    public static boolean cleanupAll(){
        List<String> pathsToClean = Arrays.asList(".git/objects/",".git/HEAD",".git/refs/heads/",".git/refs/",".git/");
        boolean success = false;

        for(String i:pathsToClean){
            File f = new File(i);
            deleteFile(f);
        }

        return success;
    }

    /**
     * Reads a file as a byte[]
     * @param path the path of the file needing to be read
     * @return the byte[] that contains the files content
     * @throws IOException
     */
    public static byte[] readFilesByteContent(String path) throws IOException {
        File file = new File(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        return fileContent;
    }


    /**
     * Converts a byte[] into a string denoted in a hexadecimal notation
     * @param bytes the byte[] that is to be converted
     * @return a String that contains the inserted byte[] in hexadecimal notation
     */
    public static String bytesToHex(byte[] bytes) {
        if(bytes.length == 0){
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }


    /**
     * Compresses inserted byte[] with gzip
     * @param content content to be compressed as byte[]
     * @return compressed content as byte[]
     * @throws IOException
     */
    public static byte[] compress(byte[] content) throws IOException{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflateOutstream = new DeflaterOutputStream(outStream);
        deflateOutstream.write(content);
        deflateOutstream.finish();
        deflateOutstream.close();
        return outStream.toByteArray();
    }


    /**
     * Decompresses a byte[] that was previously compressed with gzip
     * @param compressed byte[] that has be compressed
     * @return uncompressed byte[] of input
     * @throws IOException
     */
    public static byte[] decompress(byte[] compressed) throws IOException {
        InflaterInputStream inflater = new InflaterInputStream(
                new ByteArrayInputStream(compressed)
        );

        return inflater.readAllBytes();
    }


    /**
     * Normalizes a path, used when a filename was entered, this will add "./" or ".\" to any file thats not already noted as path
     * @param path the File in question
     * @return the path of the inserted file, either same as input if already a path, or ./input if not returns null on empty path
     */
    public static String normalizePath(String path) {
        if(StringUtils.isBlank(path)){
            return null;
        }
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

    /**
     * Computes the SHA-1 hash of the given byte array.
     *
     * @param blob the byte array to hash
     * @return computed SHA1 hash as a 20byte byte[]
     * @throws NoSuchAlgorithmException if SHA-1 algorithm is not available
     */
    public static byte[] hashBytes(byte[] blob) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(blob);

        return md.digest();
    }
}
