package com.example.phong.instagram.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by phong on 9/9/2017.
 */

public class FileSearch {
    public static ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFile = file.listFiles();
        for (int i = 0 ; i < listFile.length ; i++){
            if(listFile[i].isDirectory()) {
                pathArray.add(listFile[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFile = file.listFiles();
        for (int i = 0 ; i < listFile.length ; i++){
            if(listFile[i].isFile()) {
                pathArray.add(listFile[i].getAbsolutePath());
            }
        }
        return pathArray;
    }
}
