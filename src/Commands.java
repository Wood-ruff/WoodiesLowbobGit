import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Commands {
    public static void execute(String command)throws Exception{
        switch(command){
            case "init":
                Commands.init();
                break;
        }
    }

    private static void init() throws Exception{
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(".git"));
            writer.write("ref: refs/heads/main");
            writer.close();
        } catch (IOException e) {
            Util.cleanupAll();
            throw new Exception("Could not initialize repository, all folders were cleaned");
        }

        if(!(new File(".git").mkdir() && new File(".git/objects").mkdir())){
            Util.cleanupAll();
        }

    }
}
