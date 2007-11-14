/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.Map;

import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author xingao
 *
 */
public class PropertyListTest {

    private static WdkModel wdkModel;
    
    @BeforeClass
    public static void loadModel() throws WdkModelException {
        String projectId = System.getProperty(Utilities.SYSTEM_PROPERTY_PROJECT_ID);
        
        wdkModel = WdkModel.construct(projectId);
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
