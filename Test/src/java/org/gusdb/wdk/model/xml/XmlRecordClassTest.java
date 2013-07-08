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

    private WdkModel wdkModel;

    public XmlRecordClassTest() throws Exception {
        this.wdkModel = UnitTestHelper.getModel();
    }

    /**
     * test reading question from the model
     */
    @Test
    public void testGetAllXmlRecordClasseSets() {
        // validate the references to the record classes
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            String setName = rcSet.getName();
            Assert.assertTrue("set name", setName.trim().length() > 0);

            XmlRecordClass[] recordClasses = rcSet.getRecordClasses();
            Assert.assertTrue("record class count", recordClasses.length > 0);
            for (XmlRecordClass recordClass : recordClasses) {
                Assert.assertEquals("parent set", setName,
                        recordClass.getRecordClassSet().getName());

                String name = recordClass.getName();
                Assert.assertTrue("record class name", name.trim().length() > 0);

                Assert.assertNotNull("type", recordClass.getType());

                String fullName = recordClass.getFullName();
                Assert.assertTrue("fullName starts with",
                        fullName.startsWith(setName));
                Assert.assertTrue("fullName ends with", fullName.endsWith(name));
            }
        }
    }

    @Test
    public void testGetXmlRecordClassSet() throws WdkModelException {
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            String setName = rcSet.getName();

            XmlRecordClassSet set = wdkModel.getXmlRecordClassSet(setName);
            Assert.assertEquals(setName, set.getName());
        }
    }

    @Test(expected = WdkModelException.class)
    public void testGetInvalidXmlRecordClassSet() throws WdkModelException {
        String setName = "NonexistXmlSet";
        wdkModel.getXmlRecordClassSet(setName);
    }

    /**
     * get record class
     */
    @Test
    public void testGetXmlRecordClass() throws WdkModelException {
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            for (XmlRecordClass recordClass : rcSet.getRecordClasses()) {
                String name = recordClass.getName();

                XmlRecordClass rc = rcSet.getRecordClass(name);
                Assert.assertEquals("by name", name, rc.getName());

                String fullName = recordClass.getFullName();
                rc = (XmlRecordClass) wdkModel.resolveReference(fullName);
                Assert.assertEquals("by fullName", fullName, rc.getFullName());
            }
        }
    }

    @Test(expected = WdkModelException.class)
    public void testGetInvalidXmlRecordClass() throws WdkModelException {
        String rcName = "NonexistXmlRecordClass";
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            rcSet.getRecordClass(rcName);
        }
    }

    @Test(expected = WdkModelException.class)
    public void testGetInvalidXmlRecordClassByFullName()
            throws WdkModelException {
        String fullName = "NonexistSet.NonexistXmlRecordClass";
        wdkModel.resolveReference(fullName);
    }

    @Test
    public void testGetXmlAttributes() throws WdkModelException {
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            for (XmlRecordClass rc : rcSet.getRecordClasses()) {
                for (XmlAttributeField attribute : rc.getAttributeFields()) {
                    String name = attribute.getName();
                    Assert.assertTrue("name", name.trim().length() > 0);
                    Assert.assertTrue("display name",
                            attribute.getDisplayName().trim().length() > 0);

                    XmlAttributeField attr = rc.getAttributeField(name);
                    Assert.assertEquals("by name", name, attr.getName());
                }
            }
        }
    }

    @Test(expected = WdkModelException.class)
    public void testGetInvalidXmlAttribute() throws WdkModelException {
        String attrName = "NonexistAttribute";
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            for (XmlRecordClass rc : rcSet.getRecordClasses()) {
                rc.getAttributeField(attrName);
            }
        }
    }

    @Test
    public void testGetXmlTables() throws WdkModelException {
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            for (XmlRecordClass rc : rcSet.getRecordClasses()) {
                for (XmlTableField table : rc.getTableFields()) {
                    String name = table.getName();
                    Assert.assertTrue("name", name.trim().length() > 0);
                    Assert.assertTrue("display name",
                            table.getDisplayName().trim().length() > 0);

                    XmlTableField tbl = rc.getTableField(name);
                    Assert.assertEquals("by name", name, tbl.getName());

                    XmlAttributeField[] attributes = table.getAttributeFields();
                    Assert.assertTrue("attribute count", attributes.length > 0);
                    for (XmlAttributeField attribute : attributes) {
                        String attrName = attribute.getName();
                        XmlAttributeField attr = table.getAttributeField(attrName);
                        Assert.assertEquals("attribute", attrName,
                                attr.getName());
                    }
                }
            }
        }
    }

    @Test(expected = WdkModelException.class)
    public void testGetInvalidXmlTable() throws WdkModelException {
        String attrName = "NonexistTable";
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            for (XmlRecordClass rc : rcSet.getRecordClasses()) {
                rc.getTableField(attrName);
            }
        }
    }

    @Test(expected = WdkModelException.class)
    public void testGetInvalidXmlAttributeByTable() throws WdkModelException {
        String attrName = "NonexistTable";
        for (XmlRecordClassSet rcSet : wdkModel.getXmlRecordClassSets()) {
            for (XmlRecordClass rc : rcSet.getRecordClasses()) {
                for (XmlTableField table : rc.getTableFields()) {
                    table.getAttributeField(attrName);
                }
            }
        }
    }
}
