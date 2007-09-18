/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.util.Map;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassTest;
import org.gusdb.wdk.model.ReporterRef;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelTestBase;
import org.junit.Assert;

/**
 * @author Jerric
 * 
 */
public class ReporterTest extends WdkModelTestBase {

    public static final String SAMPLE_REPORTER = "tabular";

    /**
     * test on loading reporterRef correctly
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetReporterRef() throws WdkModelException {
        // test on tabular reporter
        String rcName = RecordClassTest.SAMPLE_RECORD_CLASS_SET + "."
                + RecordClassTest.SAMPLE_RECORD_CLASS;
        RecordClass recordClass = (RecordClass) wdkModel.resolveReference(rcName);
        ReporterRef reporterRef = recordClass.getReporterMap().get(
                SAMPLE_REPORTER);

        Assert.assertNotNull("the first recordClass from the first "
                + "recordClassSet doesn't have a tabular reporter.",
                reporterRef);
        Assert.assertEquals("the reporter's name must be " + SAMPLE_REPORTER,
                SAMPLE_REPORTER, reporterRef.getName());
        String imp = reporterRef.getImplementation();
        Assert.assertTrue("The implementation must be set", imp != null
                && imp.length() > 0);
        Map<String, String> props = reporterRef.getProperties();
        // the reporter has only one property for SampleDB
        Assert.assertEquals(1, props.size());
        for (String propName : props.keySet()) {
            Assert.assertNotNull(
                    "the property name is not initialized correctly", propName);
            Assert.assertNotNull(
                    "the property value is not initialized correctly",
                    props.get(propName));
        }
    }
}
