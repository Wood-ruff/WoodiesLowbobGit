package org.WoodiesGit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Blob {
    public static byte[] buildHash(String path) {
        try {
            byte[] content = Util.readFilesByteContent(path);
            byte[] blob = buildBlob(content);
            byte[] sha1 = Util.hashBytes(blob);

            return sha1;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static File buildBlobFile(String path) {
        try {
            byte[] content = Util.readFilesByteContent(path);
            byte[] blob = buildBlob(content);
            byte[] sha1 = Util.hashBytes(blob);
            byte[] crompressedFile = Util.compress(blob);
            String shaStr = Util.bytesToHex(sha1);
            String filename = shaStr.substring(2);
            String folder = shaStr.substring(0, 2);


            File dir = new File(String.format(".git%sobjects%s%s", File.separator, File.separator, folder));
            dir.mkdirs();
            File blobfile = new File(String.format(".git%sobjects%s%s%s%s", File.separator, File.separator, folder, File.separator, filename));
            if (blobfile.exists()) {
                System.out.println("Blob already exists, skipping");
                return blobfile;
            }

            Files.write(blobfile.toPath(), crompressedFile);

            return blobfile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] buildBlob(byte[] content) throws IOException {
        String headerstr = String.format("blob %s\0", content.length);
        byte[] header = headerstr.getBytes(StandardCharsets.UTF_8);


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(header);
        output.write(content);

        return output.toByteArray();
    }
}
