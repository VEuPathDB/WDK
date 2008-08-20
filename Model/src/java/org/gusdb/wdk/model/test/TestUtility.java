/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;
import org.xml.sax.SAXException;

/**
 * @author Jerric
 * @created Aug 29, 2005
 */
public class TestUtility {

    protected static TestUtility utility;

    protected WdkModel wdkModel;
    protected SanityModel sanityModel;
    protected Random rand;

    public static void main(String[] args) {
        String cmdName = System.getProperty("cmdName");

        Options options = declareOptions();
        CommandLine cmdLine = CommandHelper.parseOptions(cmdName, options, args);

        if (!cmdLine.hasOption("model")) {
            CommandHelper.usage(cmdName, options);
        }

        if (!cmdLine.hasOption("testCase")) {
            // no test case assigned, run all tests
            TestRunner.run(suite());
        } else {
            String testCase = cmdLine.getOptionValue("testCase");
            if (testCase.equalsIgnoreCase("JUnitUserTest")) {
                TestRunner.run(JUnitUserTest.suite());
            } else if (testCase.equalsIgnoreCase("JUnitBooleanExpressionTest")) {
                TestRunner.run(JUnitBooleanExpressionTest.suite());
            } else { // unknow test cases
                System.err.println("Unknown test case: " + testCase);
            }
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(JUnitBooleanExpressionTest.suite());
        suite.addTest(JUnitUserTest.suite());
        // suite.addTest(WdkProcessClientTest.suite());
        return suite;
    }

    public static TestUtility getInstance() throws WdkModelException,
            SAXException, IOException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        if (utility == null) utility = new TestUtility();
        return utility;
    }

    /**
     * @throws WdkModelException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * 
     */
    public TestUtility() throws WdkModelException, SAXException, IOException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        super();
        wdkModel = loadWdkModel();
        sanityModel = loadSanityModel();
        rand = new Random(System.currentTimeMillis());
    }

    /**
     * @return Returns the rand.
     */
    public Random getRandom() {
        return this.rand;
    }

    /**
     * @return Returns the sanityModel.
     */
    public SanityModel getSanityModel() {
        return this.sanityModel;
    }

    /**
     * @return Returns the wdkModel.
     */
    public WdkModel getWdkModel() {
        return this.wdkModel;
    }

    private WdkModel loadWdkModel() throws WdkModelException,
            NoSuchAlgorithmException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // load wdk model
        String modelName = System.getProperty("model");
        return WdkModel.construct(modelName);
    }

    private SanityModel loadSanityModel() throws WdkModelException,
            SAXException, IOException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String modelName = System.getProperty("model");
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        SanityTestXmlParser parser = new SanityTestXmlParser(gusHome);
        return parser.parseModel(modelName);
    }

    private static Options declareOptions() {
        String[] names = { "model", "testCase" };
        String[] descs = {
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)",
                "(Optional) The specific test case to be executed." };
        boolean[] required = { true, false };
        int[] args = { 0, 0 };

        Options options = CommandHelper.declareOptions(names, descs, required,
                args);

        return options;
    }
}
