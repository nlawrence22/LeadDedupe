import com.nlaw.leadDedupe.JsonFileUtilsImpl;
import com.nlaw.leadDedupe.Lead;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *  Tests the JsonFileUtilsImpl class methods
 *
 *  System Rules is used to handle system exit conditions, as the test would
 *  exit abnormally (and likely throw an exception) otherwise.
 */
public class JsonFileUtilsImplTest {
    private String workingdir = System.getProperty("user.dir");
    private String fileSep = System.getProperty("file.separator");
    private JsonFileUtilsImpl fileUtils;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder(
            new File(System.getProperty("user.dir")));

    @Before
    public void setUp(){
        fileUtils = new JsonFileUtilsImpl();
    }

    @Test
    public void getInputFileReturnsValidPath() throws IOException {
        File temp = folder.newFile("test.txt");
        assertEquals(temp, fileUtils.getInputFile(folder.getRoot().getName() +
                System.getProperty("file.separator") + "test.txt"));
    }

    @Test
    public void getInputFileExitsOnNull(){
        exit.expectSystemExitWithStatus(1);
        fileUtils.getInputFile(null);
    }

    @Test
    public void parseJsonReturnsCorrectNumOfItems() throws IOException {
        String testJsonPath = workingdir + fileSep + "testResources" +
                fileSep + "leads.json";
        File testJsonFile = new File(testJsonPath);

        List<Lead> parseOutput = fileUtils.parseJson(testJsonFile);

        assertEquals(10, parseOutput.size());
    }

    @Test
    public void parseJsonReturnsCorrectData() throws IOException{
        String testJsonPath = workingdir + fileSep + "testResources" +
                fileSep + "leads.json";
        File testJsonFile = new File(testJsonPath);

        List<Lead> parseOutput = fileUtils.parseJson(testJsonFile);
        Lead firstRecord = parseOutput.get(0);

        String email = firstRecord.getEmail();
        String id = firstRecord.get_id();
        String firstName = firstRecord.getFirstName();
        String lastName = firstRecord.getLastName();
        String address = firstRecord.getAddress();
        String entryDate = firstRecord.getEntryDate();

        assertEquals("jkj238238jdsnfsj23", id);
        assertEquals("foo@bar.com", email);
        assertEquals("John", firstName);
        assertEquals("Smith", lastName);
        assertEquals("123 Street St", address);
        assertEquals("2014-05-07T17:30:20+00:00", entryDate);
    }
}