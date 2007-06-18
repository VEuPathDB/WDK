/**
 * 
 */
package org.gusdb.wdk.model.implementation;

import java.util.Properties;

/**
 * @author Jerric
 * this test suite covers the tests on required environment settings.
 * - test complete environment
 * - test incomplete environment
 *      - test missing model name
 *      - test missing configDir
 *      - test missing schemaFile
 *      - test missing xmlSchemaFile
 *      - test missing xmlDataDir
 *      - test missing model-config.xml
 *      - test missing model.prop
 */
public class ModelXmlParserEnvironmentTest {

    private Properties systemProperties;

    @org.junit.Before
    public void backupEnvironment() {
        systemProperties = new Properties(System.getProperties());
    }

    @org.junit.After
    public void restoreEnvironment() {
        System.setProperties(systemProperties);
    }
}
