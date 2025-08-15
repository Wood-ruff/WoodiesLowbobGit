import javax.imageio.IIOException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

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

    public static byte[] compress(byte[] content) throws IOException{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflateOutstream = new DeflaterOutputStream(outStream);
        deflateOutstream.write(content);
        deflateOutstream.finish();
        deflateOutstream.close();
        return outStream.toByteArray();
    }
}
