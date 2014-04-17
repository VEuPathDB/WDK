/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.user.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class DatasetParamTest {

  public static List<String[]> generateRandomValues() {
    List<String[]> values = new ArrayList<>();
    Random random = UnitTestHelper.getRandom();
    int rowCount = random.nextInt(10) + 1;
    int columnCount = random.nextInt(DatasetFactory.MAX_VALUE_COLUMNS) + 1;
    for (int i = 0; i < rowCount; i++) {
      String[] row = new String[columnCount];
      for (int j = 0; j < columnCount; j++) {
        row[j] = Integer.toString(random.nextInt());
      }
      values.add(row);
    }
    return values;
  }

  public static String generateContent(List<String[]> data) {
    StringBuilder buffer = new StringBuilder();
    for (String[] row : data) {
      for (String value : row) {
        buffer.append(value).append(", ");
      }
      buffer.append("\n");
    }
    return buffer.toString();
  }


  public static void assertEquals(List<String[]> expected, List<String[]> actual) {
    Assert.assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      String[] expectedRow = expected.get(i);
      String[] actualRow = actual.get(i);
      Assert.assertArrayEquals(expectedRow, actualRow);
    }
  }

  private final User user;
  private final DatasetParam datasetParam;
  private final Random random;

  public DatasetParamTest() throws Exception {
    WdkModel wdkModel = UnitTestHelper.getModel();
    this.user = UnitTestHelper.getRegisteredUser();
    DatasetParam datasetParam = null;
    for (ParamSet paramSet : wdkModel.getAllParamSets()) {
      for (Param param : paramSet.getParams()) {
        if (param instanceof DatasetParam) {
          datasetParam = (DatasetParam) param;
          break;
        }
      }
    }
    if (datasetParam == null) {
      datasetParam = new DatasetParam();
      datasetParam.excludeResources(wdkModel.getProjectId());
      datasetParam.resolveReferences(wdkModel);
    }
    this.datasetParam = datasetParam;
    this.random = new Random();
  }

  @Test
  public void testRawToStabletValue() throws WdkModelException,
      WdkUserException {
    List<String[]> data = generateRandomValues();
    String content = generateContent(data);
    DatasetParser parser = new ListDatasetParser();
    String uploadFile = "file-" + random.nextInt();

    DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.createOrGetDataset(user, parser, content, uploadFile);
    
    Map<String, String> contextValues = new LinkedHashMap<>();

    String stableValue = datasetParam.getStableValue(user, dataset,
        contextValues);
    int datasetId = Integer.valueOf(stableValue);
    Assert.assertEquals(dataset.getDatasetId(), datasetId);
    
    Dataset dataset1 = (Dataset)datasetParam.getRawValue(user, stableValue, contextValues);

    Assert.assertEquals(dataset.getDatasetId(), dataset1.getDatasetId());
    assertEquals(data, dataset.getValues());
    Assert.assertEquals(content, dataset.getContent());
  }

  @Test
  public void testStableToRawValue() throws WdkModelException, WdkUserException {
    List<String[]> data = generateRandomValues();
    String content = generateContent(data);
    DatasetParser parser = new ListDatasetParser();
    String uploadFile = "file-" + random.nextInt();

    DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.createOrGetDataset(user, parser, content, uploadFile);

    String stableValue = Integer.toString(dataset.getDatasetId());

    Dataset rawValue = (Dataset)datasetParam.getRawValue(user, stableValue,
        null);
    Assert.assertEquals(dataset.getDatasetId(), rawValue.getDatasetId());
  }
}
