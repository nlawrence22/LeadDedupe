import com.nlaw.leadDedupe.JsonFileUtilsImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *  Tests the JsonFileUtilsImpl class methods
 *
 *  System Rules is used to handle system exit conditions, as the test would
 *  exit abnormally (and likely throw an exception) otherwise.
 */
public class JsonFileUtilsImplTest {
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


}
