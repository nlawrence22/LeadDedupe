
import com.nlaw.leadDedupe.Main;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;


/**
 *  Test Class for the Main program.
 *
 *  Some logic that only exists in the main method needs to be tested.  We use
 *  the System Rules
 */
public class MainTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

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
    public void testOneArgumentDoesNotExit(){
        Main.main(new String[] {"/path/to/test"});
    }

    @Test
    public void testTwoArgumentsDoesNotExit(){
        Main.main(new String[]{"/path/to/file /path/to/out"});
    }
}
