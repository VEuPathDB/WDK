/**
 * 
 */
package org.gusdb.wdk.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jerric
 * 
 */
public class RecordClassTest extends WdkModelTestBase {

    public static final String SAMPLE_RECORD_CLASS_SET = "SampleRecords";
    public static final String SAMPLE_RECORD_CLASS = "SampleRecord";

    /**
     * test reading question from the model
     */
    @Test
    public void testGetRecordClasses() {
        // validate the references to the record classes
        RecordClassSet[] rcSets = wdkModel.getAllRecordClassSets();
        Assert.assertTrue("There must be at least one record class set",
                rcSets.length > 0);
        for (RecordClassSet rcSet : rcSets) {
            RecordClass[] recordClasses = rcSet.getRecordClasses();
            Assert.assertTrue("There must be at least one Record class in "
                    + "each query set", recordClasses.length > 0);
            for (RecordClass recordClass : recordClasses) {
                // the record class must have at least one attribute
                Assert.assertTrue(
                        "The record class must define at least one field",
                        recordClass.getFields().length > 0);
            }
        }
    }

    /**
     * get record class
     * 
     * @throws WdkModelException
     */
    @Test
    public void testRecordClass() throws WdkModelException {
        // get record class by set
        RecordClassSet rcSet = wdkModel.getRecordClassSet(SAMPLE_RECORD_CLASS_SET);
        RecordClass rc1 = rcSet.getRecordClass(SAMPLE_RECORD_CLASS);
        Assert.assertNotNull(rc1);

        // get record class directly
        RecordClass rc2 = (RecordClass) wdkModel.resolveReference(SAMPLE_RECORD_CLASS_SET
                + "." + SAMPLE_RECORD_CLASS);
        Assert.assertSame(rc1, rc2);
        Assert.assertTrue(rc2.getFullName().startsWith(rcSet.getName()));
    }

    /**
     * get attributes that are defined in the attributeQueryRef
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetAttributesFromQueryRef() throws WdkModelException {
        RecordClass recordClass = (RecordClass) wdkModel.resolveReference(SAMPLE_RECORD_CLASS_SET
                + "." + SAMPLE_RECORD_CLASS);

        // get column attribute
        ColumnAttributeField columnField = (ColumnAttributeField) recordClass.getAttributeField("query_name");
        Assert.assertNotNull(columnField);

        // get link attribute
        LinkAttributeField linkField = (LinkAttributeField) recordClass.getAttributeField("sample_query_link");
        Assert.assertNotNull(linkField);
        Assert.assertTrue(linkField.getUrl().length() > 0);

        // get text attribute
        TextAttributeField textField = (TextAttributeField) recordClass.getAttributeField("sample_query_text");
        Assert.assertNotNull(textField);
        Assert.assertTrue(textField.getText().startsWith("The Query"));
    }

    /**
     * get the attributes from tables
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetAttributesFromTable() throws WdkModelException {
        RecordClass recordClass = (RecordClass) wdkModel.resolveReference(SAMPLE_RECORD_CLASS_SET
                + "." + SAMPLE_RECORD_CLASS);

        TableField table = recordClass.getTableField("SampleTable");
        Assert.assertNotNull(table.getQuery());
        AttributeField[] attributes = table.getAttributeFields();
        Assert.assertTrue(attributes.length > 0);
        for (AttributeField attribute : attributes) {
            Assert.assertNotNull(attribute);
        }
    }

    /**
     * get link & text attributes that are defined directly in record class
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetAttributes() throws WdkModelException {
        RecordClass recordClass = (RecordClass) wdkModel.resolveReference(SAMPLE_RECORD_CLASS_SET
                + "." + SAMPLE_RECORD_CLASS);

        // get link attribute
        LinkAttributeField linkField = (LinkAttributeField) recordClass.getAttributeField("sample_link");
        Assert.assertNotNull(linkField);
        Assert.assertTrue(linkField.getUrl().length() > 0);

        // get text attribute
        TextAttributeField textField = (TextAttributeField) recordClass.getAttributeField("sample_text");
        Assert.assertNotNull(textField);
        Assert.assertTrue(textField.getText().startsWith("The Query"));
    }

    /**
     * get the nested question that generates the single nested record
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetNestedRecord() throws WdkModelException {
        RecordClass recordClass = (RecordClass) wdkModel.resolveReference(SAMPLE_RECORD_CLASS_SET
                + "." + SAMPLE_RECORD_CLASS);

        // get nested question
        Question[] questions = recordClass.getNestedRecordQuestions();
        Assert.assertTrue(questions.length > 0);
        for (Question question : questions) {
            Assert.assertNotNull(question);
        }
    }

    /**
     * get the nested question that generates the nested record list
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetNestedRecordList() throws WdkModelException {
        RecordClass recordClass = (RecordClass) wdkModel.resolveReference(SAMPLE_RECORD_CLASS_SET
                + "." + SAMPLE_RECORD_CLASS);

        // get nested question
        Question[] questions = recordClass.getNestedRecordListQuestions();
        Assert.assertTrue(questions.length > 0);
        for (Question question : questions) {
            Assert.assertNotNull(question);
        }
    }
}
