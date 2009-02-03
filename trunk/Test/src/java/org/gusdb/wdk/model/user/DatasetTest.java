/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

import org.gusdb.wdk.model.UnitTestHelper;
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

    /**
     * @throws Exception
     * 
     */
    public DatasetTest() throws Exception {
        this.user = UnitTestHelper.getRegisteredUser();
    }

    @Test
    public void testCreateDataset() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        Random random = UnitTestHelper.getRandom();
        String[] values = new String[] { Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()) };
        Dataset dataset = user.createDataset(null, values);

        Assert.assertEquals(values.length, dataset.getSize());
        Assert.assertArrayEquals(values, dataset.getValues());
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
        Random random = UnitTestHelper.getRandom();
        String[] values = new String[] { Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()) };
        Dataset dataset = user.createDataset(uploadFile, values);

        Assert.assertEquals(uploadFile, dataset.getUploadFile());
    }

    @Test
    public void testCreateIdenticalDataset() throws NoSuchAlgorithmException, WdkUserException, WdkModelException, SQLException {
        Random random = UnitTestHelper.getRandom();
        String[] values = new String[] { Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()) };
        Dataset expected = user.createDataset(null, values);
        Dataset actual = user.createDataset(null, values);
        compareDatasets(expected, actual);
    }

    @Test
    public void testGetDatasetById() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        String[] values = new String[] { "temp1", "temp2", "temp3" };
        Dataset expected = user.createDataset(null, values);
        Dataset actual = user.getDataset(expected.getUserDatasetId());
        compareDatasets(expected, actual);
    }

    private void compareDatasets(Dataset expected, Dataset actual)
            throws WdkUserException, SQLException {
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
        Assert.assertArrayEquals(expected.getValues(), actual.getValues());
    }
}
