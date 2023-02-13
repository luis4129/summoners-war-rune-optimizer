package com.optmizer.summonerwaroptimizer.model;

import java.io.File;
import java.util.List;

public class FileFactory {

    public static List<File> getValidFiles() {
        return List.of(getValidFile());
    }

    public static File getValidFile() {
        return new File("");
    }

}
