import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Util {
    public static boolean deleteFile(File toDelete){
        boolean success = false;
        if(toDelete.exists()){
            return toDelete.delete();
        }
        return success;
    }

    public static boolean cleanupAll(){
        List<String> pathsToClean = Arrays.asList(".git/objects",".git/",".git");
        boolean success = false;

        for(String i:pathsToClean){
            File f = new File(i);
            deleteFile(f);
        }

        return success;
    }
}
