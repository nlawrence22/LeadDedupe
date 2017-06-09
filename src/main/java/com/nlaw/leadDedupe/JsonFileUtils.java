package com.nlaw.leadDedupe;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *  JsonFileUtils
 *
 *  This is a helper class for JSON and File related operations.  Since we're
 *  typically either reading JSON from a file or writing JSON to a file, it
 *  doesn't make sense to separate our JSON and File methods into separate
 *  classes at this time.
 *
 *  @author nlawrence
 *
 */
public interface JsonFileUtils {

    File getInputFile(String inputFilePath);

    File createOutputFile(String outputFilePath);

    List<Lead> parseJson(File inputFile) throws IOException;

    void writeOutputFile(File outputFile, List<Lead> outputLeads) throws IOException;
}
