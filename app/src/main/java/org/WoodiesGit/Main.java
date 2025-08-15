package org.WoodiesGit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    
    public static void main(String[] args) {
        try{
            Blob.buildBlobFile("./src/testfile.txt");
        }catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}