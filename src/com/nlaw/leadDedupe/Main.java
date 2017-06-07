package com.nlaw.leadDedupe;

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
 *  The command line program expects two arguments:
 *
 *  1. The input filepath
 *  2. The output filepath (optional)
 *
 *
 *  The output argument is optional.  If the output filepath is omitted,
 *  output will be written to output.json in the parent directory of the
 *  program.
 *
 *  @author nlawrence
 *
 */
public class Main {

    public static void main(String[] args) {
	// write your code here
        if (args.length <= 0 || args.length > 2 || args[0] == null){
            System.out.println("Please provide 1 or 2 arguments:");
            System.out.println("/path/to/input.json /path/to/output.json");
            System.exit(1);
        }
    }
}
