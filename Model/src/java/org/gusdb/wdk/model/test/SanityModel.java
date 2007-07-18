package org.gusdb.wdk.model.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;

import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * SanityModel.java
 * 
 * Model used in a sanity test. Contains all sanity elements representing
 * records and queries that will be excercised over the course of the sanity
 * test.
 * 
 * Created: Mon August 23 12:00:00 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-09 20:38:53 -0400 (Tue, 09 Aug
 *          2005) $Author$
 */

public class SanityModel extends WdkModelBase {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * SanityRecords contained in this model.
     */
    private List<SanityRecord> sanityRecords = new ArrayList<SanityRecord>();

    /**
     * SanityQueries contained in this model.
     */
    private List<SanityQuery> sanityQueries = new ArrayList<SanityQuery>();

    /**
     * SanityQuestions contained in this model.
     */
    private List<SanityQuestion> sanityQuestions = new ArrayList<SanityQuestion>();

    private List<SanityXmlQuestion> sanityXmlQuestions = new ArrayList<SanityXmlQuestion>();

    /**
     * Instance object used by SanityTestXmlParser to create a SanityModel if no
     * schema is provided.
     */
    public static final SanityModel INSTANCE = new SanityModel();

    // ------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------

    // SanityRecord Sets
    public void addSanityRecord(SanityRecord sanityRecord)
            throws WdkModelException {
        sanityRecords.add(sanityRecord);
    }

    public SanityRecord[] getAllSanityRecords() {

        SanityRecord records[] = new SanityRecord[sanityRecords.size()];
        sanityRecords.toArray(records);
        return records;
    }

    /**
     * @param recordName
     *        Two-part name (recordSetName.recordName) of the record in
     *        question.
     * @param return
     *        True if the model contains one or more SanityRecords for the given
     *        recordName.
     */
    public boolean hasSanityRecord(String recordName) {

        for (int i = 0; i < sanityRecords.size(); i++) {
            SanityRecord nextRecord = (SanityRecord) sanityRecords.get(i);
            if (nextRecord.getRef().equals(recordName)) return true;
        }
        return false;
    }

    // SanityQuery Sets
    public void addSanityQuery(SanityQuery sanityQuery)
            throws WdkModelException {
        sanityQueries.add(sanityQuery);
    }

    public SanityQuery[] getAllSanityQueries() {
        SanityQuery queries[] = new SanityQuery[sanityQueries.size()];
        sanityQueries.toArray(queries);
        return queries;
    }

    /**
     * @param queryName
     *        Two-part name (querySetName.queryName) of the query in question.
     * @param return
     *        True if the model contains one or more SanityQueries for the given
     *        queryName.
     */
    public boolean hasSanityQuery(String queryName) {

        for (int i = 0; i < sanityQueries.size(); i++) {
            SanityQueryOrQuestion nextQuery = (SanityQueryOrQuestion) sanityQueries.get(i);
            if (nextQuery.getRef().equals(queryName)) return true;
        }
        return false;
    }

    public void addSanityQuestion(SanityQuestion sanityQuestion)
            throws WdkModelException {

        sanityQuestions.add(sanityQuestion);
    }

    public SanityQuestion[] getAllSanityQuestions() {

        SanityQuestion questions[] = new SanityQuestion[sanityQuestions.size()];
        sanityQuestions.toArray(questions);
        return questions;
    }

    /**
     * @param queryName
     *        Two-part name (querySetName.queryName) of the query in question.
     * @param return
     *        True if the model contains one or more SanityQuestions for the
     *        given queryName.
     */
    public boolean hasSanityQuestion(String queryName) {

        for (int i = 0; i < sanityQuestions.size(); i++) {
            SanityQueryOrQuestion nextQuery = (SanityQueryOrQuestion) sanityQuestions.get(i);
            if (nextQuery.getRef().equals(queryName)) return true;
        }
        return false;
    }

    public void addSanityXmlQuestion(SanityXmlQuestion question) {
        sanityXmlQuestions.add(question);
    }

    public SanityXmlQuestion[] getSanityXmlQuestions() {
        SanityXmlQuestion[] questions = new SanityXmlQuestion[sanityXmlQuestions.size()];
        sanityXmlQuestions.toArray(questions);
        return questions;
    }

    public boolean hasSanityXmlQuestion(String ref) {
        for (SanityXmlQuestion question : sanityXmlQuestions) {
            if (question.getRef().equals(ref)) return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer result = new StringBuffer("SanityModel\nSanityQueries:\n");
        SanityQueryOrQuestion queries[] = getAllSanityQueries();
        if (queries != null) {
            for (int i = 0; i < queries.length; i++) {
                SanityQueryOrQuestion nextSanityQuery = queries[i];
                result.append(nextSanityQuery.toString() + "\n");
            }
        }
        result.append("SanityRecords:\n");
        SanityRecord records[] = getAllSanityRecords();
        if (records != null) {
            for (int i = 0; i < records.length; i++) {
                SanityRecord nextSanityRecord = records[i];
                result.append(nextSanityRecord.toString() + "\n");
            }
        }
        return result.toString();
    }

    void validateQueries() throws WdkUserException {
        validateQueriesOrQuestions(sanityQueries);
    }

    void validateQuestions() throws WdkUserException {
        validateQueriesOrQuestions(sanityQuestions);
    }

    // ------------------------------------------------------------------
    // Protected Methods
    // ------------------------------------------------------------------
    private void validateQueriesOrQuestions(List v) throws WdkUserException {

        for (int i = 0; i < v.size(); i++) {
            SanityQueryOrQuestion q = (SanityQueryOrQuestion) v.get(i);
            if (q.getMinOutputLength().intValue() < 0) {
                throw new WdkUserException("Sanity" + q.getTypeCap() + " "
                        + q.getRef() + " must return at least 0 rows. Please "
                        + "set its minOutputLength attribute to reflect this");
            }
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude queries
        List<SanityQuery> newQueries = new ArrayList<SanityQuery>();
        for (SanityQuery query : sanityQueries) {
            if (query.include(projectId)) {
                query.excludeResources(projectId);
                newQueries.add(query);
            }
        }
        sanityQueries = newQueries;

        // exclude questions
        List<SanityQuestion> newQuestions = new ArrayList<SanityQuestion>();
        for (SanityQuestion question : sanityQuestions) {
            if (question.include(projectId)) {
                question.excludeResources(projectId);
                newQuestions.add(question);
            }
        }
        sanityQuestions = newQuestions;

        // exclude records
        List<SanityRecord> newRecords = new ArrayList<SanityRecord>();
        for (SanityRecord record : sanityRecords) {
            if (record.include(projectId)) {
                record.excludeResources(projectId);
                newRecords.add(record);
            }
        }
        sanityRecords = newRecords;

        // exclude xml questions
        List<SanityXmlQuestion> newXmlQuestions = new ArrayList<SanityXmlQuestion>();
        for (SanityXmlQuestion question : sanityXmlQuestions) {
            if (question.include(projectId)) {
                question.excludeResources(projectId);
                newXmlQuestions.add(question);
            }
        }
        sanityXmlQuestions = newXmlQuestions;
    }
}
