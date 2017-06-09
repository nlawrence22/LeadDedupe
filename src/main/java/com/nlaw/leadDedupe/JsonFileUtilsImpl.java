package com.nlaw.leadDedupe;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Handles all file and JSON processing needs.
 *
 * Separate methods are provided to create and read/write files so that the
 * program can attempt to create files first, and handle those exceptions
 * before trying to operate on those files themselves (likely after some
 * significant processing has occurred).
 *
 * @author nlawrence
 *
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
            // no need to bail here, just use a default filename if that fails,
            // an exception will bubble up and the program will terminate
            System.out.println("No output filename was provided or the " +
                    "provided filename resulted in a null filepath\n" +
                    "Setting to default filename: " + defaultOutputFileName );
            outputJson = new File(defaultPath);
        }

        try{
            if (outputJson.exists()){
                //we want clean output, so remove any existing file
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

        //consume first curly brace {
        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            String name = jsonReader.nextName();
            //our leads are buried inside an array in a "leads" object
            if (name.equals("leads")){
                //consume the first bracket [
                jsonReader.beginArray();
                while (jsonReader.hasNext()){
                    Lead lead = gson.fromJson(jsonReader, Lead.class);
                    leads.add(lead);
                }
                //consume closing bracket ]
                jsonReader.endArray();
            }
        }
        //consume closing curly brace }
        jsonReader.endObject();
        jsonReader.close();
        return leads;
    }

    public void writeOutputFile(File outputFile, List<Lead> outputLeads) throws IOException {
        JsonWriter writer = new JsonWriter(new FileWriter(outputFile));
        Gson gson = new Gson();

        writer.setIndent("  ");
        writer.beginObject();
        // we need the same format, so we need to re-create the "leads" object
        // and bury our leads inside an array in that object
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
