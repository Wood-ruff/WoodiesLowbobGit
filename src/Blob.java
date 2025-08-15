import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Blob {
    public static File buildBlobFile(String path){
        try{
            byte[] content = Util.readFilesByteContent(path);
            byte[] blob = buildBlob(content);
            byte[] sha1 = hashBytes(blob);
            byte[] crompressedFile =Util.compress(blob);
            String shaStr = bytesToHex(sha1);
            String filename = shaStr.substring(2);
            String folder = shaStr.substring(0,2);


            File dir = new File(String.format(".git%sobjects%s%s", File.separator, File.separator, folder));
            dir.mkdirs();
            File blobfile = new File(String.format(".git%sobjects%s%s%s%s",File.separator,File.separator,folder,File.separator,filename));
            Files.write(blobfile.toPath(),crompressedFile);

            return blobfile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private static byte[] buildBlob(byte[] content)throws IOException{
        String headerstr = String.format("blob %s\0",content.length);
        byte[] header = headerstr.getBytes(StandardCharsets.UTF_8);


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(header);
        output.write(content);

        return output.toByteArray();
    }

    private static byte[] hashBytes(byte[] blob)throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(blob);

        return md.digest();
    }



}
