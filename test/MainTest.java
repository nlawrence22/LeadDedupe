
import com.nlaw.leadDedupe.Main;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 *  Test Class for the Main program.
 *
 *  Some logic that only exists in the main method needs to be tested.  We use
 *  the System Rules
 */
public class MainTest {
    private String workingdir = System.getProperty("user.dir");
    private String fileSep = System.getProperty("file.separator");
    private String testResourcePath = "testResources" + fileSep;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder(
            new File(workingdir));

    @Test
    public void testZeroArgumentsExits(){
        exit.expectSystemExitWithStatus(1);
        Main.main(new String[0]);
    }

    @Test
    public void threeArgumentsExits(){
        exit.expectSystemExitWithStatus(1);
        Main.main(new String[] {"/path/to/file", "/path/to/out", "garbage"});
    }


    @Test
    public void testEndToEndSystemProvidesProperOutputWithOnlyInputArg() throws IOException {
        String outputPath = workingdir + fileSep + "output.json";
        String inputPath = testResourcePath + "leads.json";
        File outputFile = new File(outputPath);
        File expectedFile = new File(testResourcePath + "integrationTestOutput.json");

        if (outputFile.exists()){
            outputFile.delete();
        }

        Main.main(new String[]{inputPath});

        List<String> expected = Files.readAllLines(expectedFile.toPath());
        List<String> actual = Files.readAllLines(outputFile.toPath());

        //verify we have the same number of lines
        assertEquals(expected.size(), actual.size());

        //then verify that each of those lines have the same content
        for (int i = 0; i < expected.size(); i ++){
            assertEquals(expected.get(i), actual.get(i));
        }
    }
    @Test
    public void testEndToEndSystemProvidesProperOutput() throws IOException {

        String outputPath = folder.getRoot().getName() + fileSep + "testOutput.json";
        String inputPath = testResourcePath + "leads.json";
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        File expectedFile = new File(testResourcePath + "integrationTestOutput.json");

        Main.main(new String[]{inputPath, outputPath});

        List<String> expected = Files.readAllLines(expectedFile.toPath());
        List<String> actual = Files.readAllLines(outputFile.toPath());

        //verify we have the same number of lines
        assertEquals(expected.size(), actual.size());

        //then verify that each of those lines have the same content
        for (int i = 0; i < expected.size(); i ++){
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
