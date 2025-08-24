package org.WoodiesGit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commands {
    public static void execute(String command) throws Exception {
        Pattern p = Pattern.compile("git (\\w+)(.*)");
        Matcher m = p.matcher(command);
        if (!m.matches()) {
            System.err.println("Couldnt interpret command");
            return;
        }
        String type = m.group(1);

        switch (type) {
            case "init":
                Commands.init();
                break;

            case "add":
                Commands.add(m.group(2).trim());
                break;
        }
    }

    private static void add(String files) {
        String[] paths = files.split(" ");
        Index.updateIndex(paths);
        for (String path : paths) {
            if (path.startsWith(""))
                Blob.buildBlobFile(path);
        }
    }

    private static void init() throws Exception {
        if (new File(".git").exists()) {
            System.out.println("Already a git repository");
            return;
        }

        boolean success = new File(".git").mkdirs() &&
                new File(".git/objects").mkdirs() &&
                new File(".git/refs").mkdirs() &&
                new File(".git/refs/heads").mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(".git/HEAD"));) {
            writer.write("ref: refs/heads/main\n");
        } catch (IOException e) {
            throw new Exception("Could not initialize repository");
        }

        if (success) {
            System.out.println("Successfully initialized Git repository");
        } else {
            System.out.println("Git repository could not fully be created");
        }
    }
}
