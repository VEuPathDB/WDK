/**
 * @description
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.json.JSONException;
import org.junit.Assert;

/**
 * @author Jerric
 * @created Jul 5, 2007
 * @modified Jul 5, 2007
 */
public class ParamTest extends WdkModelTestBase {

    public static final String SAMPLE_PARAM_SET = "params";
    public static final String SAMPLE_STRING_PARAM = "primaryKey";
    public static final String SAMPLE_ENUM_PARAM = "booleanParam";
    public static final String SAMPLE_FLAT_VOCAB_PARAM = "flatVocabParam";

    /**
     * test getting param sets and params from the model
     */
    @org.junit.Test
    public void testGetAllParams() {
        ParamSet[] paramSets = wdkModel.getAllParamSets();
        Assert.assertTrue("There must be at least one param set",
                paramSets != null && paramSets.length > 0);
        for (ParamSet paramSet : paramSets) {
            Assert.assertNotNull("the param set should not be null", paramSet);
            Param[] params = paramSet.getParams();
            Assert.assertTrue(
                    "There must be at least one param in the param set "
                            + paramSet.getName(), params != null
                            && params.length > 0);
            for (Param param : params) {
                Assert.assertNotNull("the param should not be null", param);
            }
        }
    }

    /**
     * test getting a param set
     * 
     * @throws WdkUserException
     */
    @org.junit.Test
    public void testGetParamSet() throws WdkModelException {
        ParamSet paramSet = wdkModel.getParamSet(SAMPLE_PARAM_SET);
        Assert.assertNotNull(paramSet);
        // the useTermOnly is set to true
        Assert.assertTrue(paramSet.isUseTermOnly());
    }

    /**
     * test getting an invalid paramSet that doesn't belong to SampleDB
     * 
     * @throws WdkUserException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidParamSet() throws WdkModelException {
        wdkModel.getParamSet("toyParams");
    }

    /**
     * test getting param in different ways
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetParam() throws WdkModelException {
        ParamSet paramSet = wdkModel.getParamSet(SAMPLE_PARAM_SET);
        Param param1 = paramSet.getParam(SAMPLE_FLAT_VOCAB_PARAM);
        Assert.assertNotNull(param1);

        Param param2 = (Param) wdkModel.resolveReference(SAMPLE_PARAM_SET + "."
                + SAMPLE_FLAT_VOCAB_PARAM);
        Assert.assertSame(param1, param2);

        String help = param1.getHelp();
        Assert.assertTrue(help != null && help.trim().length() > 0);
    }

    /**
     * test getting a stringParam, anf verify its sample and default value. The
     * historyParam and datasetParam is considered tested since they are very
     * similar to stringParam
     * 
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @org.junit.Test
    public void testGetStringParam() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        StringParam param = (StringParam) wdkModel.resolveReference(SAMPLE_PARAM_SET
                + "." + SAMPLE_STRING_PARAM);
        Assert.assertNotNull(param);

        String sample = param.getSample();
        Assert.assertTrue(sample != null && sample.trim().length() > 0);
        String defaultValue = param.getDefault();
        Assert.assertTrue(defaultValue != null
                && defaultValue.trim().length() > 0);
    }

    /**
     * test getting a flat vocab param, and iterate on its terms and internals
     * 
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @org.junit.Test
    public void testGetFlatVocabParam() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        FlatVocabParam param = (FlatVocabParam) wdkModel.resolveReference(SAMPLE_PARAM_SET
                + "." + SAMPLE_FLAT_VOCAB_PARAM);
        Assert.assertNotNull(param);
        Assert.assertTrue(param.isUseTermOnly());

        // get terms and internals
        String[] terms = param.getVocab();
        String[] internals = param.getVocabInternal();
        Assert.assertTrue(terms.length > 0);

        // terms and internals are equal, since the useTermOnly is true
        Assert.assertArrayEquals(terms, internals);

        // get the default value
        Assert.assertEquals("Optional 1,Optional 3", param.getDefault());
    }

    /**
     * test getting an enum param and iterate on its terms and internals
     * 
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @org.junit.Test
    public void testGetEnumParam() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        EnumParam param = (EnumParam) wdkModel.resolveReference(SAMPLE_PARAM_SET
                + "." + SAMPLE_ENUM_PARAM);
        Assert.assertNotNull(param);
        Assert.assertFalse(param.isUseTermOnly());

        // get terms and internals
        String[] terms = param.getVocab();
        String[] internals = param.getVocabInternal();
        String[] displays = param.getDisplays();
        Assert.assertTrue(terms.length == 2 && terms.length == internals.length);

        // terms and internals are not equal, since the useTermOnly is false
        for (int i = 0; i < terms.length; i++) {
            Assert.assertFalse(terms[i] == null
                    || terms[i].equals(internals[i]));
        }

        // verify displays
        Assert.assertTrue(displays.length == terms.length);
        for (String display : displays) {
            Assert.assertNotNull(display);
        }

        // verify the default value
        Assert.assertEquals("False", param.getDefault());
    }
}
