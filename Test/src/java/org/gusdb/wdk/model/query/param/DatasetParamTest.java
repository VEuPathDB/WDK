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
import org.gusdb.wdk.model.query.param.dataset.DatasetParam;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.DatasetFactory;
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

  public static String generateRawValue(List<String[]> data) {
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
  private final DatasetFactory factory;
  private final Random random;

  public DatasetParamTest() throws Exception {
    WdkModel wdkModel = UnitTestHelper.getModel();
    this.user = UnitTestHelper.getRegisteredUser();
    this.factory = wdkModel.getDatasetFactory();
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
    String rawValue = generateRawValue(data);
    String type = "text";
    String uploadFile = "file-" + random.nextInt();

    Map<String, String> contextValues = new LinkedHashMap<>();
    contextValues.put(datasetParam.getFileSubParam(), uploadFile);
    contextValues.put(datasetParam.getTypeSubParam(), type);

    String stableValue = datasetParam.getStableValue(user, rawValue,
        contextValues);
    int userDatasetId = Integer.valueOf(stableValue);
    Dataset dataset = factory.getDataset(user, userDatasetId);

    Assert.assertEquals(userDatasetId, dataset.getUserDatasetId());
    Assert.assertEquals(rawValue, dataset.getOriginalContent());
    Assert.assertEquals(type, dataset.getContentType());
    assertEquals(data, dataset.getValues());
  }

  @Test
  public void testStableToRawValue() throws WdkModelException, WdkUserException {
    List<String[]> data = generateRandomValues();
    String rawValue = generateRawValue(data);
    String type = "text";
    String uploadFile = "file-" + random.nextInt();

    Map<String, String> contextValues = new LinkedHashMap<>();
    contextValues.put(datasetParam.getFileSubParam(), uploadFile);
    contextValues.put(datasetParam.getTypeSubParam(), type);

    String stableValue = datasetParam.getStableValue(user, rawValue,
        contextValues);

    String actualRaw = datasetParam.getRawValue(user, stableValue,
        contextValues);
    Assert.assertEquals(rawValue, actualRaw);
  }
}
