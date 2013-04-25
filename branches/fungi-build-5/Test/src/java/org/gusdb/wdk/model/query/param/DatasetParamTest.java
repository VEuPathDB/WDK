/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class DatasetParamTest {

    private User user;
    private DatasetParam datasetParam;
    private RecordClass recordClass;

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
        this.recordClass = datasetParam.getRecordClass();
    }

    @Test
    public void testRawToDependentValue() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        String values = generateRandomValues();
        Dataset dataset = user.createDataset(recordClass, null, values);

        String rawValue = dataset.getValue();
        String dependentValue = datasetParam.rawValueToDependentValue(user,
                null, rawValue);
        Assert.assertEquals(Integer.toString(dataset.getUserDatasetId()),
                dependentValue);
    }

    @Test
    public void testSpacedRawToDependentValue()
            throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException {
        String values = generateRandomValues();
        Dataset dataset = user.createDataset(recordClass, null, values);

        String dependentValue = datasetParam.rawValueToDependentValue(user,
                null, values);
        Assert.assertEquals(Integer.toString(dataset.getUserDatasetId()),
                dependentValue);
    }

    @Test
    public void testRawOrDependentToDependentValue()
            throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, JSONException {
        String values = generateRandomValues();
        Dataset dataset = user.createDataset(recordClass, null, values);
        String expected = Integer.toString(dataset.getUserDatasetId());

        // first, try raw value
        String rawValue = dataset.getValue();
        String dependentValue = datasetParam.rawOrDependentValueToDependentValue(
                user, rawValue);
        Assert.assertEquals(expected, dependentValue);

        // second, try dependent value
        dependentValue = datasetParam.rawOrDependentValueToDependentValue(user,
                expected);
        Assert.assertEquals(expected, dependentValue);
    }

    private String generateRandomValues() {
        StringBuffer buffer = new StringBuffer();
        Random random = UnitTestHelper.getRandom();
        int rowCount = random.nextInt(10) + 1;
        int columnCount = recordClass.getPrimaryKeyAttributeField().getColumnRefs().length;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                if (j > 0) buffer.append(", ");
                buffer.append(random.nextInt());
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
