/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class DatasetParamTest {

    private User user;
    private Dataset dataset;
    private DatasetParam datasetParam;

    public DatasetParamTest() throws Exception {
        this.user = UnitTestHelper.getRegisteredUser();

        WdkModel wdkModel = UnitTestHelper.getModel();
        for (ParamSet paramSet : wdkModel.getAllParamSets()) {
            for (Param param : paramSet.getParams()) {
                if (param instanceof DatasetParam) {
                    this.datasetParam = (DatasetParam) param;
                    break;
                }
            }
        }
    }

    @Test
    public void testRawToDependentValue() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        Random random = UnitTestHelper.getRandom();
        String[] values = new String[] { Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()) };
        Dataset dataset = user.createDataset(null, values);

        String rawValue = Utilities.fromArray(values);
        String dependentValue = datasetParam.rawValueToDependentValue(user,
                null, rawValue);
        Assert.assertEquals(Integer.toString(dataset.getUserDatasetId()),
                dependentValue);
    }

    @Test
    public void testRawOrDependentToDependentValue()
            throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException {
        Random random = UnitTestHelper.getRandom();
        String[] values = new String[] { Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()) };
        Dataset dataset = user.createDataset(null, values);
        String expected = Integer.toString(dataset.getUserDatasetId());

        // first, try raw value
        String rawValue = Utilities.fromArray(values);
        String dependentValue = datasetParam.rawOrDependentValueToDependentValue(
                user, rawValue);
        Assert.assertEquals(expected, dependentValue);

        // second, try dependent value
        dependentValue = datasetParam.rawOrDependentValueToDependentValue(user,
                expected);
        Assert.assertEquals(expected, dependentValue);
    }
}
