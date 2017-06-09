package com.nlaw.leadDedupe;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
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
        File outputJson;
        String defaultPath = workingDir + pathSeparator + defaultOutputFileName;

        try{
            if (outputFilePath == null){
                throw new NullPointerException("Provided Filename was null");
            }

            String path = workingDir + pathSeparator + outputFilePath;
            outputJson = new File(path);

        } catch (NullPointerException e) {
            //TODO: Log errors to separate file
            System.out.println("No output filename was provided or the provided " +
                    "filename resulted in a null filepath\n" +
                    "Setting to default filename: " + defaultOutputFileName );
            outputJson = new File(defaultPath);
        }

        try{
            if (outputJson.exists()){
                outputJson.delete();
            }
            outputJson.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating output file: ");
            e.printStackTrace();
            System.out.println("Aborting!");
            //We can't really continue if we can't write the output file either
            System.exit(1);
        }

        return outputJson;
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

    public void writeOutputFile(File outputFile, List<Lead> outputLeads) throws IOException {
        JsonWriter writer = new JsonWriter(new FileWriter(outputFile));
        Gson gson = new Gson();

        writer.setIndent("  ");
        writer.beginObject();
        writer.name("leads");
        writer.beginArray();
        for (Lead lead:outputLeads) {
            gson.toJson(lead, Lead.class, writer);
        }
        writer.endArray();
        writer.endObject();
        writer.close();
    }
}
