package org.WoodiesGit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Blob {

    /**
     * builds just the hash of the blobbed file
     * @param path path of the file that the hash is required of
     * @return SHA1 hash of the file passed as a 20byte byte[]
     */
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

    /**
     * builds the full blob file and saves it to the correct spot as per gits specifications
     * @param path path of the file that needs to be "blobbed"
     * @return fileinstance of the file that was created int he /objects folder
     */
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


    /**
     * builds the uncompressed unhashed full version of the blob
     * @param content content of the file as byte[]
     * @return full blob as a byte[]
     * @throws IOException
     */
    public static byte[] buildBlob(byte[] content) throws IOException {
        String headerstr = String.format("blob %s\0", content.length);
        byte[] header = headerstr.getBytes(StandardCharsets.UTF_8);


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(header);
        output.write(content);

        return output.toByteArray();
    }
}
