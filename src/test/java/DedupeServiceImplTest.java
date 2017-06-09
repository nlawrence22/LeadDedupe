import com.nlaw.leadDedupe.DedupeServiceImpl;
import com.nlaw.leadDedupe.Lead;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * Tests the DedupeService implementation
 *
 * We currently only use the example data we were given, plus some
 * examples of each possible three way collision situation we could
 * run into.
 *
 */
public class DedupeServiceImplTest {
    private String workingdir = System.getProperty("user.dir");
    private String fileSep = System.getProperty("file.separator");
    private String testResourcePath = "src" + fileSep + "test" + fileSep +
            "resources" + fileSep;
    private DedupeServiceImpl dedupeService;

    @Before
    public void setUp(){
        dedupeService = new DedupeServiceImpl();
    }

    @Test
    public void testDeduplicateItemsReturnsCorrectNumberRecords() {
        String testJsonPath = testResourcePath + "leads.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertEquals(5, dedupeOutput.size());
    }

    @Test
    public void testDeduplicateItemsContainsNoDuplicateEmails(){
        String testJsonPath = testResourcePath + "leads.json";
        Set emails = new HashSet();

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        for (Lead lead:dedupeOutput) {
            String email = lead.getEmail();
            assertFalse(emails.contains(email));
            emails.add(email);
        }
    }

    @Test
    public void testDeduplicateItemsContainsNoDuplicateIDs(){
        String testJsonPath = testResourcePath + "leads.json";
        Set ids = new HashSet();

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        for (Lead lead:dedupeOutput){
            String id = lead.get_id();
            assertFalse(ids.contains(id));
            ids.add(id);
        }
    }

    @Test
    public void testThreeWayCollision123(){
        String testJsonPath = testResourcePath + "leadsThreeWay123.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("bog@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollision132(){
        String testJsonPath = testResourcePath + "leadsThreeWay132.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("foo@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollision213(){
        String testJsonPath = testResourcePath + "leadsThreeWay213.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("bog@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollision231(){
        String testJsonPath = testResourcePath + "leadsThreeWay231.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("wabaj238238jdsnfsj23", lead.get_id());
        assertEquals("bog@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollision312(){
        String testJsonPath = testResourcePath + "leadsThreeWay312.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("foo@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollision321(){
        String testJsonPath = testResourcePath + "leadsThreeWay321.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 2);

        Lead firstLead = dedupeOutput.get(0);
        assertEquals("wabaj238238jdsnfsj23", firstLead.get_id());
        assertEquals("bog@bar.com", firstLead.getEmail());

        Lead secondLead = dedupeOutput.get(1);
        assertEquals("sel045238jdsnfsj23", secondLead.get_id());
        assertEquals("foo@bar.com", secondLead.getEmail());
    }

    @Test
    public void testThreeWayCollisionTwoYounger(){
        String testJsonPath = testResourcePath + "leadsThreeWayTwoYounger.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("bog@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollisionTwoOlder(){
        String testJsonPath = testResourcePath + "leadsThreeWayTwoOlder.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("foo@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollisionThreeYounger(){
        String testJsonPath = testResourcePath + "leadsThreeWayThreeYounger.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 2);

        Lead firstLead = dedupeOutput.get(0);
        assertEquals("wabaj238238jdsnfsj23", firstLead.get_id());
        assertEquals("bog@bar.com", firstLead.getEmail());

        Lead secondLead = dedupeOutput.get(1);
        assertEquals("sel045238jdsnfsj23", secondLead.get_id());
        assertEquals("foo@bar.com", secondLead.getEmail());
    }

    @Test
    public void testThreeWayCollisionThreeOlder(){
        String testJsonPath = testResourcePath + "leadsThreeWayThreeOlder.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("bog@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollisionOneYounger(){
        String testJsonPath = testResourcePath + "leadsThreeWayOneYounger.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("sel045238jdsnfsj23", lead.get_id());
        assertEquals("bog@bar.com", lead.getEmail());
    }

    @Test
    public void testThreeWayCollisionOneOlder(){
        String testJsonPath = testResourcePath + "leadsThreeWayOneOlder.json";

        List<Lead> dedupeOutput = dedupeService.deduplicateItems(testJsonPath);

        assertTrue(dedupeOutput.size() == 1);
        Lead lead = dedupeOutput.get(0);
        assertEquals("wabaj238238jdsnfsj23", lead.get_id());
        assertEquals("bog@bar.com", lead.getEmail());
    }
}
