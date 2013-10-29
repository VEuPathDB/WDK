/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class DatasetParamHandler extends AbstractParamHandler {

  public static final String PROP_PARSERS = "parsers";

  private final Map<String, DatasetParser> parsers = new LinkedHashMap<>();

  public Collection<DatasetParser> getParsers() {
    return parsers.values();
  }

  /**
   * the parsers are loaded while setting the properties.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.AbstractParamHandler#setProperties(java.util.Map)
   */
  @Override
  public void setProperties(Map<String, String> properties)
      throws WdkModelException {
    super.setProperties(properties);

    String parserNames = properties.get(PROP_PARSERS);
    if (parserNames != null && parserNames.length() > 0) {
      // create all the parsers
      for (String parserName : parserNames.split(",")) {
        try {
          Class<? extends DatasetParser> parserClass = Class.forName(parserName).asSubclass(
              DatasetParser.class);
          DatasetParser parser = parserClass.newInstance();
          parsers.put(parser.getName(), parser);
        } catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException ex) {
          throw new WdkModelException(ex);
        }

        // get parser specific params
        for (String propName : properties.keySet()) {
          String propValue = properties.get(propName);
          String[] parts = propName.split("\\.", 2);
          if (parts.length != 0)
            continue;
          String prefix = parts[0];
          DatasetParser parser = parsers.get(prefix);
          if (parser != null)
            parser.addProperty(parts[1], propValue);
        }
      }
    }
  }

  /**
   * The raw value is any kind of arbitrary text, and depending on the type
   * sub-param of the input, we will choose different parsers to process the raw
   * value into stable value. The stable value is a user_dataset_id
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toStableValue(org.gusdb
   *      .wdk.model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, String rawValue,
      Map<String, String> contextValues) throws WdkUserException {
    // get type and find a proper parser for it
    DatasetParam datasetParam = (DatasetParam) param;
    String type = contextValues.get(datasetParam.getTypeSubParam());
    String uploadFile = contextValues.get(datasetParam.getFileSubParam());
    DatasetParser parser = parsers.get(type);

    // parse the content;
    List<String[]> data = parser.parse(rawValue);

    // save the data into database, and get user dataset id
    DatasetFactory datasetFactory = param.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.getDataset(user, data, rawValue, type,
        uploadFile);

    return Integer.toString(dataset.getUserDatasetId());
  }

  /**
   * The stable value is user_dataset_id, the raw value is the original content.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toRawValue(org.gusdb.wdk.model
   *      .user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toRawValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException {
    int userDatasetId = Integer.valueOf(stableValue);
    DatasetFactory datasetFactory = param.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.getDataset(user, userDatasetId);
    return dataset.getContent();
  }

  /**
   * the internal value is an SQL that queries against the dataset values.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.
   *      wdk.model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException {
    if (param.isNoTranslation())
      return stableValue;

    ModelConfig config = wdkModel.getModelConfig();
    String dbLink = config.getAppDB().getUserDbLink();
    String wdkSchema = config.getUserDB().getWdkEngineSchema();
    String userSchema = config.getUserDB().getUserSchema();
    String dvTable = wdkSchema + DatasetFactory.TABLE_DATASET_VALUE + dbLink;
    String udTable = userSchema + DatasetFactory.TABLE_USER_DATASET + dbLink;
    String colDatasetId = DatasetFactory.COLUMN_DATASET_ID;
    String colUserDatasetId = DatasetFactory.COLUMN_USER_DATASET_ID;
    StringBuffer sql = new StringBuffer("SELECT dv.* FROM ");
    sql.append(udTable + " ud, " + dvTable + " dv ");
    sql.append(" WHERE dv." + colDatasetId + " = ud." + colDatasetId);
    sql.append(" AND ud." + colUserDatasetId + " = " + stableValue);
    return sql.toString();
  }

  /**
   * The signature is the dataset id, which is independent from user info.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.
   *      model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException {
    int userDatasetId = Integer.valueOf(stableValue);
    DatasetFactory datasetFactory = param.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.getDataset(user, userDatasetId);
    return Integer.toString(dataset.getDatasetId());
  }

}
