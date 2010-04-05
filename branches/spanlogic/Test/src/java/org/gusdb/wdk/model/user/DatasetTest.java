/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class DatasetTest {

    private User user;
    private RecordClass recordClass;

    /**
     * @throws Exception
     * 
     */
    public DatasetTest() throws Exception {
        this.user = UnitTestHelper.getRegisteredUser();
        WdkModel wdkModel = UnitTestHelper.getModel();
        recordClass = wdkModel.getAllRecordClassSets()[0].getRecordClasses()[0];
    }

    @Test
    public void testCreateDataset() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        String values = generateRandomValues();

        Dataset dataset = user.createDataset(recordClass, null, values);

        Assert.assertTrue(dataset.getDatasetId() > 0);
        Assert.assertTrue(dataset.getUserDatasetId() > 0);
        Assert.assertNotNull(dataset.getChecksum());
        Assert.assertNotNull(dataset.getSummary());
        Assert.assertNotNull(dataset.getValue());
        Assert.assertEquals(user.getUserId(), dataset.getUser().getUserId());
        Assert.assertNull(dataset.getUploadFile());
    }

    @Test
    public void testCreateDatasetWithUpload() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        String uploadFile = "sample.file";
        String values = generateRandomValues();
        Dataset dataset = user.createDataset(recordClass, uploadFile, values);

        Assert.assertEquals(uploadFile, dataset.getUploadFile());
    }

    @Test
    public void testCreateIdenticalDataset() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        String values = generateRandomValues();
        Dataset expected = user.createDataset(recordClass, null, values);
        Dataset actual = user.createDataset(recordClass, null, values);
        compareDatasets(expected, actual);
    }

    @Test
    public void testGetDatasetById() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        String values = generateRandomValues();
        Dataset expected = user.createDataset(recordClass, null, values);
        Dataset actual = user.getDataset(expected.getUserDatasetId());
        compareDatasets(expected, actual);
    }

    @Test
    public void testCreateIdenticalDatasetFromUsers() throws Exception {
        String values = generateRandomValues();
        Dataset expected = user.createDataset(recordClass, null, values);

        User guest = UnitTestHelper.getGuest();
        Dataset actual = guest.createDataset(recordClass, null, values);

        Assert.assertEquals(guest.getUserId(), actual.getUser().getUserId());
        Assert.assertEquals(expected.getDatasetId(), actual.getDatasetId());
        Assert.assertEquals(expected.getChecksum(), actual.getChecksum());
        Assert.assertEquals(expected.getValue(), actual.getValue());
    }

    private void compareDatasets(Dataset expected, Dataset actual)
            throws WdkUserException, SQLException, WdkModelException {
        Assert.assertEquals(expected.getChecksum(), actual.getChecksum());
        Assert.assertEquals(expected.getSummary(), actual.getSummary());
        Assert.assertEquals(expected.getUploadFile(), actual.getUploadFile());
        Assert.assertEquals(expected.getValue(), actual.getValue());
        Assert.assertEquals(expected.getCreateTime(), actual.getCreateTime());
        Assert.assertEquals(expected.getDatasetId(), actual.getDatasetId());
        Assert.assertEquals(expected.getSize(), actual.getSize());
        Assert.assertEquals(expected.getUser().getUserId(),
                actual.getUser().getUserId());
        Assert.assertEquals(expected.getUserDatasetId(),
                actual.getUserDatasetId());
        Assert.assertEquals(expected.getValue(), actual.getValue());
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
