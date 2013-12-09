/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.List;
import java.util.Random;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DatasetParamTest;
import org.gusdb.wdk.model.query.param.dataset.ListDatasetParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class DatasetTest {

  private final User user;
  private final DatasetFactory datasetFactory;
  private final Random random;

  public DatasetTest() throws WdkModelException, WdkUserException {
    this.user = UnitTestHelper.getRegisteredUser();
    WdkModel wdkModel = UnitTestHelper.getModel();
    this.datasetFactory = wdkModel.getDatasetFactory();
    this.random = new Random();
  }

  @Test
  public void testCreateDataset() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateRawValue(data);
    String type = new ListDatasetParser().getName();
    String uploadFile = null;

    Dataset dataset = datasetFactory.createOrGetDataset(user, data, content,
        type, uploadFile);

    Assert.assertTrue(dataset.getDatasetId() > 0);
    Assert.assertTrue(dataset.getUserDatasetId() > 0);
    Assert.assertNotNull(dataset.getDatasetChecksum());
    Assert.assertNotNull(dataset.getContentChecksum());
    Assert.assertEquals(user.getUserId(), dataset.getUser().getUserId());
    Assert.assertEquals(type, dataset.getContentType());
    Assert.assertNull(dataset.getUploadFile());
    Assert.assertEquals(content, dataset.getOriginalContent());
    DatasetParamTest.assertEquals(data, dataset.getValues());
  }

  @Test
  public void testCreateDatasetWithUpload() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateRawValue(data);
    String type = new ListDatasetParser().getName();
    String uploadFile = "sample-" + random.nextInt();
    Dataset dataset = datasetFactory.createOrGetDataset(user, data, content,
        type, uploadFile);

    Assert.assertEquals(uploadFile, dataset.getUploadFile());
  }

  @Test
  public void testCreateIdenticalDataset() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateRawValue(data);
    String type = new ListDatasetParser().getName();
    String uploadFile = "sample-" + random.nextInt();
    Dataset expected = datasetFactory.createOrGetDataset(user, data, content,
        type, uploadFile);
    Dataset actual = datasetFactory.createOrGetDataset(user, data, content,
        type, uploadFile);
    compareDatasets(expected, actual, true);
  }

  @Test
  public void testGetDatasetById() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateRawValue(data);
    String type = new ListDatasetParser().getName();
    String uploadFile = "sample-" + random.nextInt();
    Dataset expected = datasetFactory.createOrGetDataset(user, data, content,
        type, uploadFile);
    Dataset actual = user.getDataset(expected.getUserDatasetId());
    compareDatasets(expected, actual, true);
  }

  @Test
  public void testCreateIdenticalDatasetFromUsers() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateRawValue(data);
    String type = new ListDatasetParser().getName();
    String uploadFile = "sample-" + random.nextInt();
    Dataset expected = datasetFactory.createOrGetDataset(user, data, content,
        type, uploadFile);

    User guest = UnitTestHelper.getGuest();
    String uploadFile2 = "sample-" + random.nextInt();
    Dataset actual = datasetFactory.createOrGetDataset(guest, data, content,
        type, uploadFile2);

    compareDatasets(expected, actual, false);
  }

  private void compareDatasets(Dataset expected, Dataset actual,
      boolean sameUser) throws WdkModelException {
    if (sameUser) {
      Assert.assertEquals(expected.getUser().getUserId(),
          actual.getUser().getUserId());
      Assert.assertEquals(expected.getUserDatasetId(),
          actual.getUserDatasetId());
      Assert.assertEquals(expected.getUploadFile(), actual.getUploadFile());
    }
    Assert.assertEquals(expected.getDatasetId(), actual.getDatasetId());
    Assert.assertEquals(expected.getDatasetChecksum(),
        actual.getDatasetChecksum());
    Assert.assertEquals(expected.getContentChecksum(),
        actual.getContentChecksum());
    Assert.assertEquals(expected.getContentType(), actual.getContentType());
    Assert.assertEquals(expected.getOriginalContent(),
        actual.getOriginalContent());
    Assert.assertEquals(expected.getCreateTime(), actual.getCreateTime());
    Assert.assertEquals(expected.getSize(), actual.getSize());
    Assert.assertEquals(expected.getUser().getUserId(),
        actual.getUser().getUserId());
    DatasetParamTest.assertEquals(expected.getValues(), actual.getValues());
  }
}
