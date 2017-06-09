package com.nlaw.leadDedupe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  DedupeServiceImpl
 *
 *  The main bulk of deduplication happens here.  Implements the DedupeService.
 *
 *  We use a list of original items, originalLeads, which is derived from the
 *  Json input file as our base data.  We then track items for output in our
 *  outputLeads list.
 *
 *  For faster lookups, we keep track of the email addresses and ids from the
 *  output list in two hashmaps, emailMap and indexMap.  The emailMap maps ids
 *  to email.  The indexMap maps emails to an array index, which represents the
 *  location of the lead associated with that email in the outputLeads list.
 *  While this requires a lot of space, this helps us keep our runtime down to
 *  something somewhat reasonable in the common case.  This would not be
 *  sustainable for large inputs.
 *
 *  @author nlawrence
 *
 */
public class DedupeServiceImpl implements DedupeService {

    public static final Logger logger = LogManager.getLogger(DedupeServiceImpl.class.getName());

    private File inputJson;

    private JsonFileUtilsImpl fileUtils = new JsonFileUtilsImpl();

    private List<Lead> originalLeads;
    private List<Lead> outputLeads = new ArrayList<>(); // our list of unique leads
    private Map<String,String> emailMap = new HashMap<>(); //maps id to email
    private Map<String,Integer> indexMap = new HashMap<>(); // maps email to index


    /**
     *
     *  We take in a file, parse it for Json, then iterate through each item in
     *  the json list to deduplicate according to specific rules.  Those rules
     *  are:
     *
     *  1. The data from the newest date should be preferred
     *  2. Duplicate IDs count as dupes. Duplicate emails count as dupes. Other
     *     duplicate values do not constitute a duplicate record.
     *  3. If dates are identical, use the record provided last in the list.
     *
     *  When the data has been successfully deduplicated, we return a list of
     *  unique objects.
     *
     *  There are three main cases we'll run into when examining a record:
     *
     *  1. The email address and ID of the record do not match a record we have
     *  chosen to keep up to this point.
     *  2. The email address, ID, or both match one existing record we've chosen
     *  to keep.
     *  3. The email address and ID match two separate existing records.
     *
     *  In the first case, we simply add the item to the list.  In the second,
     *  we compare the dates of the item using rules 1 and 3 as our guide and
     *  update the existing item if necessary.
     *
     *  The third case, however, is tricky.  The assumption this implementation
     *  uses is that based on our rules, we should only keep one record, and
     *  that should be the newest record.  We toss out the other two records
     *  involved in the three-way collision.
     *
     * @param inputFilename The filename of the input file
     * @return A list of unique (deduplicated) Leads
     */
    public List<Lead> deduplicateItems(String inputFilename) {

        inputJson = fileUtils.getInputFile(inputFilename);
        try{
            originalLeads = fileUtils.parseJson(inputJson);
        } catch (IOException e){
            System.out.println("Could not parse input Json");
            e.printStackTrace();
            System.out.println("Aborting!");
            System.exit(1);
        }


        // Iterate through each item, and merge it into the output list
        for (Lead lead : originalLeads){
            logger.debug("Processing new record...");
            String email = lead.getEmail();
            String id = lead.get_id();

            boolean idExists = emailMap.containsKey(id);
            boolean emailExists = indexMap.containsKey(email);

            if (!idExists && !emailExists){
                //CASE: Neither Emails nor ids match an existing record
                addNewRecord(lead, email, id);

            } else if (idExists){
                int itemIndex = indexMap.get(emailMap.get(id)); //IDs match automatically
                Lead idMatchRecord = outputLeads.get(itemIndex);
                boolean emailsMatch = lead.getEmail().equals(idMatchRecord.getEmail());

                if (emailsMatch || !emailExists){
                    // CASE: Either both the ids and emails match
                    // OR
                    // CASE: emails don't match, but no existing item with the same
                    // email as the new record.
                    // in either case, we've only collided with one record
                    mergeWithExistingRecord(lead, itemIndex, idMatchRecord);
                } else {
                    // CASE: The id's match but the emails don't match
                    // AND
                    // CASE: there's already an existing record with that email
                    // So, we've collided with two records.
                    mergeThreeRecords(lead, email, idMatchRecord);
                    continue;
                }
            } else if (emailExists){
                // CASE: Email exists, IDs don't (because we don't currently
                // have this id listed)
                int itemIndex = indexMap.get(email);
                Lead emailMatchRecord = outputLeads.get(itemIndex);
                mergeWithExistingRecord(lead, itemIndex, emailMatchRecord);
            }
        }
        return outputLeads;
    }

    /**
     *
     * Adds a new record, i.e. an email and id that we don't currently have in
     * the list, to the output list for later serialization to JSON and logs
     * that change to the list.
     *
     * @param lead The lead to be added to the list.
     * @param email The email address associated with the lead to be added.
     * @param id The id of the lead to be added.
     */
    private void addNewRecord(Lead lead, String email, String id) {
        int arrayIndex = outputLeads.size();

        indexMap.put(email, arrayIndex);
        emailMap.put(id,email);
        logger.debug("Adding Record: \n" + "  " + lead.toString());
        outputLeads.add(arrayIndex, lead);
    }

    /**
     *
     * Handles a collision between a new record and one existing record.
     *
     * We compare the dates of the current/new record and the existing record
     * using the rules:
     *
     * 1. The data from the newest date should be preferred
     * 3. If dates are identical, use the record provided last in the list.
     *
     * If the new record is preferred over the old record, then the existing record is
     * replaced by the new record and the changes are logged.  If the existing record
     * is preferred, then the new record is simply discarded.
     *
     * @param lead The new record.
     * @param itemIndex The index of the existing record in the output list.
     * @param originalRecord The existing record.
     */
    private void mergeWithExistingRecord(Lead lead, int itemIndex, Lead originalRecord) {
        OffsetDateTime origDate = OffsetDateTime.parse(originalRecord.getEntryDate());
        OffsetDateTime leadDate = OffsetDateTime.parse(lead.getEntryDate());

        /* Since the new record is later in the file, if the dates are equal
           then we update the same as if the date is newer, aka after the
           date currently mapped. */
        if (leadDate.isAfter(origDate) || leadDate.isEqual(origDate)) {

            logChanges(lead, originalRecord);
            outputLeads.remove(itemIndex);

            String originalRecordEmail = originalRecord.getEmail();
            String leadEmail = lead.getEmail();

            // because we only collide with one record, it's possible that
            // either the id or the email aren't actually in the map, so
            // check first.
            if (indexMap.containsKey(originalRecordEmail)){
                indexMap.remove(originalRecordEmail);
                indexMap.put(leadEmail, itemIndex);
            }
            String originalRecord_id = originalRecord.get_id();
            if (emailMap.containsKey(originalRecord_id)){
                emailMap.remove(originalRecord_id);
                emailMap.put(lead.get_id(), leadEmail);
            }
            outputLeads.add(itemIndex, lead);
        }
    }

    /**
     *
     * Handles a collision between a new record and two existing records.
     *
     * The assumption here is that collisions should be handled in chronological
     * order.
     *
     * There are 4 main cases we can encounter, with most having additional
     * sub-cases.
     *
     * If the new record is newer than both existing records, or is equal to
     * both (by date) then we keep the new record only and toss the two
     * existing records.
     *
     * If the new record is between the two existing records, we evaluate the
     * order of the existing records in the original list. The thought here is
     * that either:
     *
     * 1. If the oldest of the existing items comes first, then the new record
     *    would have first replaced the oldest existing record, and then would
     *    have subsequently been replaced by the other existing record,
     * OR
     * 2. If the newest existing record was earlier in the list, then the
     *    collision between the oldest existing record and the new (or middle)
     *    record would have never occurred, therefore leaving the two existing
     *    records in the list (since the middle record would have been discarded
     *    when compared with the newest existing record).
     *
     * If the record is tied by date with only one of the existing records then
     * we again evaluate the order of the records in the list, such that:
     *
     * 1. If either of the existing records is younger than the lead, then a
     *    middle collision would have occurred and we toss out both existing
     *    records in favor of the new (as the new record comes second in the
     *    file after the existing record it's tied with by date).
     * OR
     * 2. If one of the existing records is older than the lead date, then
     *    we toss the existing record that's further down in the list and
     *    the new record.
     *
     * Finally, if the newest record is older than both records, then we need to
     * verify if the first two records are tied by date.  If they are, we do
     * nothing, as they both supersede the current record.  If they are not,
     * then we need to evaluate the position of the records in the list:
     *
     * 1. If the newer existing item was in the file first, then both records
     *    supersede the current record and we do nothing.
     * OR
     * 2. If the older existing item was in the file first, we remove it, as
     *    it would have been replaced by the current record.
     *
     * @param lead The new record.
     * @param email The email associated with the new record.
     * @param idMatchRecord The first existing record which collides with the
     *                       new record by id.
     */
    private void mergeThreeRecords(Lead lead, String email, Lead idMatchRecord) {
        Lead emailMatchRecord = outputLeads.get(indexMap.get(email));

        OffsetDateTime leadDate = OffsetDateTime.parse(lead.getEntryDate());
        OffsetDateTime idMatchDate = OffsetDateTime.parse(idMatchRecord.getEntryDate());
        OffsetDateTime emailMatchDate = OffsetDateTime.parse(emailMatchRecord.getEntryDate());

        String idMatchID = idMatchRecord.get_id();
        String idMatchEmail = idMatchRecord.getEmail();
        int idMatchIndex = indexMap.get(idMatchEmail);

        String emailMatchID = emailMatchRecord.get_id();
        String emailMatchEmail = emailMatchRecord.getEmail();
        int emailMatchIndex = indexMap.get(emailMatchEmail);

        int idMatchFileLocation = originalLeads.indexOf(idMatchRecord);
        int emailMatchFileLocation = originalLeads.indexOf(emailMatchRecord);

        logger.info("3-way collision!\n Current record:\n  " + lead.toString()
                + "\n Existing record with same email:\n  " + emailMatchRecord.toString()
                + "\n Exisiting record with same id:\n  " + idMatchRecord.toString());

        // if the lead date is after both dates or is equal to both dates then
        // keep the new record only.
        if ((leadDate.isAfter(idMatchDate) && leadDate.isAfter(emailMatchDate)) ||
                (leadDate.isEqual(idMatchDate) && leadDate.isEqual(emailMatchDate))){
            // remove both existing records
            removeRecord(idMatchIndex, idMatchEmail, idMatchID);

            // we've removed an item so all of the items from the original
            // index onwards have shifted down one.  Items before the original
            // index are unaffected.
            if (emailMatchIndex > idMatchIndex){
                emailMatchIndex--;
            }
            removeRecord(emailMatchIndex, emailMatchID, emailMatchEmail);

            // add current record
            addNewRecord(lead, lead.getEmail(), lead.get_id());

            // update all the indices because we've completely jacked our map
            updateIndices(0);

            return;

        } else if (leadDate.isAfter(idMatchDate) && leadDate.isBefore(emailMatchDate) ||
                (leadDate.isAfter(emailMatchDate) && leadDate.isBefore(idMatchDate))){
            //if the lead date is in the middle

            if (idMatchDate.isAfter(emailMatchDate)){
                // emailMatch < lead < idMatch
                removeRecord(emailMatchIndex, emailMatchEmail, emailMatchID);
                updateIndices(idMatchIndex);

            } else {
                // idMatch < lead < emailMatch
                removeRecord(idMatchIndex, idMatchEmail, idMatchID);
                updateIndices(emailMatchIndex);
            }

            // discard the old record as it's also too old (no-op)
            return;

        } else if (leadDate.isBefore(idMatchDate) && leadDate.isBefore(emailMatchDate)){
            // lead date is first
            // lead < email == id or lead < email < id or lead < id < email

            /* lead < email < id and id match comes first
               lead < id < email and email match comes first in the file
               in any three of the cases, the middle collision occurred.
               */
            if ((emailMatchDate.isBefore(idMatchDate) && emailMatchFileLocation < idMatchFileLocation) ||
                    (idMatchDate.isBefore(emailMatchDate) && idMatchFileLocation < emailMatchFileLocation)){
                // remove the first chronological instance, because the middle collision occurred.
                if (emailMatchDate.isBefore(idMatchDate)){

                    removeRecord(emailMatchIndex, emailMatchEmail, emailMatchID);
                    updateIndices(emailMatchIndex);

                } else {
                    removeRecord(idMatchIndex, idMatchEmail, idMatchID);
                    updateIndices(idMatchIndex);
                }
                return;
            } else {
                // because the lead would have been replaced by the first
                // of the two existing records that are encountered, and the two
                // existing records do not collide, we pass.
            }

            // discard current record because it's also too old (no-op)
            return;

        } else if ((leadDate.isEqual(idMatchDate) && !leadDate.isEqual(emailMatchDate))||
                (leadDate.isEqual(emailMatchDate) && !leadDate.isEqual(idMatchDate))){
            //lead date tied with one of existing records

                // if the first in the record in the list is younger or the second
                // record in the list is younger, then remove both existing records
            if ((emailMatchDate.isBefore(leadDate) || idMatchDate.isBefore(leadDate))){
                //remove both

                removeRecord(emailMatchIndex, emailMatchEmail, emailMatchID);

                // we've removed an item so all of the items from the original
                // index onwards have shifted down one.  Items before the original
                // index are unaffected.
                if (idMatchIndex > emailMatchIndex){
                    idMatchIndex--;
                }

                removeRecord(idMatchIndex, idMatchEmail, idMatchID);

                addNewRecord(lead, email, lead.get_id());

                // update all the indices because the map is jacked
                updateIndices(0);

                return;

            } else if ((emailMatchDate.isAfter(leadDate) && emailMatchFileLocation < idMatchFileLocation) ||
                    (idMatchDate.isAfter(leadDate) && idMatchFileLocation < emailMatchFileLocation)){
                // otherwise if the first record in the list is older, remove the second

                if (emailMatchDate.isAfter(leadDate)){
                    removeRecord(idMatchIndex, idMatchEmail, idMatchID);
                    updateIndices(idMatchIndex);
                } else {
                    removeRecord(emailMatchIndex, emailMatchEmail, emailMatchID);
                    updateIndices(emailMatchIndex);
                }
                return;
            } else {
                // otherwise if the second record in the list is older
                // remove the first
                if (emailMatchFileLocation < idMatchFileLocation){
                    removeRecord(emailMatchIndex, emailMatchEmail, emailMatchID);
                    updateIndices(emailMatchIndex);
                } else {
                    removeRecord(idMatchIndex, idMatchEmail, idMatchID);
                    updateIndices(idMatchIndex);
                }
                return;
            }
        }
    }

    /**
     *
     * Removes a single record from the output list and all tracking
     * maps.
     *
     * @param index The index of the item to be removed from the output list
     * @param email The email associated with the item removed from the list
     * @param id The id associated with the item removed from the list
     */
    private void removeRecord(int index, String email, String id){
        Lead record = outputLeads.get(index);
        outputLeads.remove(index);
        indexMap.remove(email);
        emailMap.remove(id);
        logger.debug("Removing Record: \n" + "  " + record.toString());
    }

    /**
     *
     * Updates the indexMap after removal of items where a replacement item is
     * not available.
     *
     * Because removing items from an ArrayList causes the items in the list to
     * shift to the left, if we don't immediately replace the item at that index
     * (thus causing everything to shift back to the right), our indexMap will be
     * inconsistent. This method resolves that inconsistency by iterating through
     * the map from a given index and remapping all the items.
     *
     * This can cause our overall runtime to spike to at least O(n^2) unfortunately.
     *
     * @param StartIndex The index of the item that was removed, or 0 to update all
     *                   items.
     */
    private void updateIndices(int StartIndex){

        for (int i = StartIndex; i < outputLeads.size(); i++){
            Lead currentLead = outputLeads.get(i);
            String email = currentLead.getEmail();

            indexMap.remove(email);
            indexMap.put(email, i);
        }
    }

    private void logChanges(Lead oldRecord, Lead newRecord){
        String valueChangeString = "  %s changed -- Value From: \"%s\" --> " +
                "Value To: \"%s\" \n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Merging one record:\n");
        stringBuilder.append("  Old Record:\n  " + oldRecord.toString() + "\n");
        stringBuilder.append("  New Record:\n  " + newRecord.toString() + "\n");

        if (!oldRecord.get_id().equals(newRecord.get_id())){
            stringBuilder.append(String.format(valueChangeString, "id",
                    oldRecord.get_id(), newRecord.get_id()));
        }
        if (!oldRecord.getEmail().equals(newRecord.getEmail())){
            stringBuilder.append(String.format(valueChangeString, "email",
                    oldRecord.getEmail(), newRecord.getEmail()));
        }
        if (!oldRecord.getFirstName().equals(newRecord.getFirstName())){
            stringBuilder.append(String.format(valueChangeString, "firstName",
                    oldRecord.getFirstName(), newRecord.getFirstName()));
        }
        if (!oldRecord.getLastName().equals(newRecord.getLastName())){
            stringBuilder.append(String.format(valueChangeString, "lastName",
                    oldRecord.getLastName(), newRecord.getLastName()));
        }
        if (!oldRecord.getAddress().equals(newRecord.getAddress())){
            stringBuilder.append(String.format(valueChangeString, "address",
                    oldRecord.getAddress(), newRecord.getAddress()));
        }
        if (!oldRecord.getEntryDate().equals(newRecord.getEntryDate())){
            stringBuilder.append(String.format(valueChangeString, "entryDate",
                    oldRecord.getEntryDate(), newRecord.getEntryDate()));
        }
        logger.info(stringBuilder.toString());
    }
}
