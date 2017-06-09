package com.nlaw.leadDedupe;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *  LeadDeduper
 *
 *  A small utility for de-duplicating JSON lists of leads with a specific
 *  input format.
 *
 *  The utility takes in a file of JSON formatted data, removes duplicates
 *  based on specific rules, logs the changes and then outputs the sanitized
 *  list.  The rules are:
 *
 *  1. The data from the newest date should be preferred
 *  2. Duplicate IDs count as dupes. Duplicate emails count as dupes. Other
 *     duplicate values do not constitute a duplicate record.
 *  3. If dates are identical, use the record provided last in the list.
 *
 *  The command line program expects one argument, and up to a second,
 *  optional argument:
 *
 *  1. The input filename relative to the working directory of the program
 *  2. The output filepath relative to the working directory of the program (optional)
 *
 *  The output argument is optional.  If the output filepath is omitted,
 *  output will be written to output.json in the parent directory of the
 *  program.
 *
 *  @author nlawrence
 *
 */
public class Main {
    public static String usage = "Usage:\n" +
            "java -jar leadDedupe-[version].jar input_filename [output_filename]";

    public static void main(String[] args) {
	    // Check arguments before doing anything else no need to waste memory
        // or time if we've got bogus input

        if (args.length <= 0 || args.length > 2 || args[0] == null){
            System.out.println("Please provide 1 or 2 arguments");
            System.out.println(usage);
            System.exit(1);
        }

        String inputPath;
        String outputPath;

        inputPath = args[0];
        // don't try to assign an index out of bounds...
        outputPath = args.length == 2 ? args[1] : null;

        JsonFileUtilsImpl fileUtils = new JsonFileUtilsImpl();
        // We should try to create the output file first, because we'll waste
        // time if we do all the work and can't write the file at the end.
        File outputFile = fileUtils.createOutputFile(outputPath);

        DedupeService deduper = new DedupeServiceImpl();
        List<Lead> outputLeads = deduper.deduplicateItems(inputPath);

        try {
            fileUtils.writeOutputFile(outputFile, outputLeads);
        } catch (IOException e) {
            System.out.println("Unable to write output file!");
            e.printStackTrace();
            System.out.println("Changes should be tracked in logs");
            System.exit(1);
        }

        System.out.println("Done! Output file is at " + outputFile.getAbsolutePath().toString());
    }
}
