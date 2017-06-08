package com.nlaw.leadDedupe;

import java.io.File;
import java.util.List;

/**
 * Created by nlawrence on 6/7/17.
 */
public class JsonFileUtilsImpl implements JsonFileUtils {
    public static final String defaultOutputFileName = "output.json";

    private String workingDir = System.getProperty("user.dir");
    private String pathSeparator = System.getProperty("file.separator");


    public File getInputFile(String inputFilePath) {
        File inputJson = null;
        try{
            if (inputFilePath == null){
                // Can't proceed without a filename for our input file.
                throw new NullPointerException("Provided input filename was null.");
            }
            String path = workingDir + pathSeparator + inputFilePath;

            inputJson = new File(path);
        } catch (Exception e) {
            //TODO: Log errors to separate file
            System.out.println("The provided input filename resulted in a null path, aborting");
            e.printStackTrace();
            System.exit(1);
        }

        return inputJson;
     }

    public File createOutputFile(String outputFilePath){
        return null;
    }

    public List<Lead> parseJson(File inputFile) {
        return null;
    }

    public void writeOutputFile() {

    }
}
