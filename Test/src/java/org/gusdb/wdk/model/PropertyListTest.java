/**
 * 
 */
package org.gusdb.wdk.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class PropertyListTest {

    private static WdkModel wdkModel;

    @BeforeClass
    public static void loadModel() throws WdkModelException,
            NoSuchAlgorithmException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String projectId = System.getProperty(Utilities.ARGUMENT_PROJECT_ID);
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        wdkModel = WdkModel.construct(projectId, gusHome);
    }

    @Test
    public void testGetEmptyPropertyList() throws WdkModelException {
        Question question = (Question) wdkModel.resolveReference("GeneQuestions.GenesByEcNumber");

        final String plName = "specificAttribution";
        String[] properties = question.getPropertyList(plName);
        assertEquals("property size", 0, properties.length);

        Map<String, String[]> propertyMap = question.getPropertyLists();
        properties = propertyMap.get(plName);
        assertEquals("property size", 0, properties.length);
    }
}
