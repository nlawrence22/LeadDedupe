package com.nlaw.leadDedupe;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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


    public List<Lead> parseJson(File inputFile) throws IOException {
        List<Lead> leads = new ArrayList<>();
        JsonReader jsonReader = new JsonReader(new FileReader(inputFile));
        Gson gson = new Gson();

        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            String name = jsonReader.nextName();
            if (name.equals("leads")){
                jsonReader.beginArray();
                while (jsonReader.hasNext()){
                    Lead lead = gson.fromJson(jsonReader, Lead.class);
                    leads.add(lead);
                }
                jsonReader.endArray();
            }
        }
        jsonReader.endObject();
        jsonReader.close();
        return leads;
    }

    public void writeOutputFile() {

    }
}
