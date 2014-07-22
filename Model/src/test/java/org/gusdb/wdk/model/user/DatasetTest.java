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
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParamTest;
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
  private final DatasetParser parser;

  public DatasetTest() throws WdkModelException, WdkUserException {
    this.user = UnitTestHelper.getRegisteredUser();
    WdkModel wdkModel = UnitTestHelper.getModel();
    this.datasetFactory = wdkModel.getDatasetFactory();
    this.random = new Random();
    this.parser = new ListDatasetParser();
  }

  @Test
  public void testCreateDataset() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateContent(data);

    Dataset dataset = datasetFactory.createOrGetDataset(user, parser, content, null);

    Assert.assertTrue(dataset.getDatasetId() > 0);
    Assert.assertNotNull(dataset.getChecksum());
    Assert.assertEquals(user.getUserId(), dataset.getUser().getUserId());
    Assert.assertEquals(parser.getName(), dataset.getParserName());
    Assert.assertNull(dataset.getUploadFile());
    Assert.assertEquals(content, dataset.getContent());
    DatasetParamTest.assertEquals(data, dataset.getValues());
  }

  @Test
  public void testCreateDatasetWithUpload() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateContent(data);
    String uploadFile = "sample-" + random.nextInt();
    Dataset dataset = datasetFactory.createOrGetDataset(user, parser, content, uploadFile);

    Assert.assertEquals(uploadFile, dataset.getUploadFile());
  }

  @Test
  public void testCreateIdenticalDataset() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateContent(data);
    String uploadFile = "sample-" + random.nextInt();
    Dataset expected = datasetFactory.createOrGetDataset(user, parser, content, uploadFile);
    Dataset actual = datasetFactory.createOrGetDataset(user, parser, content, uploadFile);
    compareDatasets(expected, actual, true);
  }

  @Test
  public void testGetDatasetById() throws Exception {
    List<String[]> data = DatasetParamTest.generateRandomValues();
    String content = DatasetParamTest.generateContent(data);
    String uploadFile = "sample-" + random.nextInt();
    Dataset expected = datasetFactory.createOrGetDataset(user, parser, content, uploadFile);
    Dataset actual = user.getDataset(expected.getDatasetId());
    compareDatasets(expected, actual, true);
  }

  private void compareDatasets(Dataset expected, Dataset actual, boolean sameUser) throws WdkModelException {
    if (sameUser) {
      Assert.assertEquals(expected.getUser().getUserId(), actual.getUser().getUserId());
      Assert.assertEquals(expected.getDatasetId(), actual.getDatasetId());
      Assert.assertEquals(expected.getUploadFile(), actual.getUploadFile());
    }
    Assert.assertEquals(expected.getDatasetId(), actual.getDatasetId());
    Assert.assertEquals(expected.getChecksum(), actual.getChecksum());
    Assert.assertEquals(expected.getParserName(), actual.getParserName());
    Assert.assertEquals(expected.getContent(), actual.getContent());
    Assert.assertEquals(expected.getCreatedTime(), actual.getCreatedTime());
    Assert.assertEquals(expected.getSize(), actual.getSize());
    Assert.assertEquals(expected.getUser().getUserId(), actual.getUser().getUserId());
    DatasetParamTest.assertEquals(expected.getValues(), actual.getValues());
  }
}
