/**
 * 
 */
package org.gusdb.wdk.model.xml;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jerric
 * 
 */
public class XmlRecordClassTest {

    private static final String SAMPLE_XML_RECORD_CLASS_SET = "XmlRecordClasses";
    private static final String SAMPLE_XML_RECORD_CLASS = "NewsRecord";

    private WdkModel wdkModel;

    public XmlRecordClassTest() throws Exception {
        this.wdkModel = UnitTestHelper.getModel();
    }

    /**
     * test reading question from the model
     */
    @Test
    public void testGetXmlRecordClasses() {
        // validate the references to the record classes
        XmlRecordClassSet[] rcSets = wdkModel.getXmlRecordClassSets();
        Assert.assertTrue("There must be at least one record class set",
                rcSets.length > 0);
        for (XmlRecordClassSet rcSet : rcSets) {
            XmlRecordClass[] recordClasses = rcSet.getRecordClasses();
            Assert.assertTrue("There must be at least one Record class in "
                    + "each query set", recordClasses.length > 0);
            for (XmlRecordClass recordClass : recordClasses) {
                // the record class must have at least one attribute
                Assert.assertTrue(
                        "The record class must define at least one field",
                        recordClass.getAttributeFields().length > 0
                                || recordClass.getTableFields().length > 0);
            }
        }
    }

    /**
     * get record class
     * 
     * @throws WdkModelException
     */
    @Test
    public void testXmlRecordClass() throws WdkModelException {
        // get record class by set
        XmlRecordClassSet rcSet = wdkModel.getXmlRecordClassSet(SAMPLE_XML_RECORD_CLASS_SET);
        XmlRecordClass rc1 = rcSet.getRecordClass(SAMPLE_XML_RECORD_CLASS);
        Assert.assertNotNull(rc1);

        // get record class directly
        XmlRecordClass rc2 = (XmlRecordClass) wdkModel.resolveReference(SAMPLE_XML_RECORD_CLASS_SET
                + "." + SAMPLE_XML_RECORD_CLASS);
        Assert.assertSame(rc1, rc2);
        Assert.assertTrue(rc2.getFullName().startsWith(rcSet.getName()));
    }

    /**
     * get the attributes from tables
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetXmlAttributesFromTable() throws WdkModelException {
        XmlRecordClass recordClass = (XmlRecordClass) wdkModel.resolveReference(SAMPLE_XML_RECORD_CLASS_SET
                + "." + SAMPLE_XML_RECORD_CLASS);

        XmlTableField table = recordClass.getTableField("relatedLinks");
        XmlAttributeField[] attributes = table.getAttributeFields();
        Assert.assertTrue(attributes.length > 0);
        for (XmlAttributeField attribute : attributes) {
            Assert.assertNotNull(attribute);
        }
    }

    /**
     * get link & text attributes that are defined directly in record class
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetXmlAttributes() throws WdkModelException {
        XmlRecordClass recordClass = (XmlRecordClass) wdkModel.resolveReference(SAMPLE_XML_RECORD_CLASS_SET
                + "." + SAMPLE_XML_RECORD_CLASS);

        // get xml attribute
        XmlAttributeField linkField = recordClass.getAttributeField("headline");
        Assert.assertNotNull(linkField);
    }
}
