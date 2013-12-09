/**
 * 
 */
package org.gusdb.wdk.model.query.param.dataset;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractParamHandler;
import org.gusdb.wdk.model.query.param.ParamHandler;
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
          if (parts.length != 0) continue;
          String prefix = parts[0];
          DatasetParser parser = parsers.get(prefix);
          if (parser != null) parser.addProperty(parts[1], propValue);
        }
      }
    }
  }

  /**
   * The raw value is any kind of arbitrary text, and depending on the type
   * sub-param of the input, we will choose different parsers to process the raw
   * value into stable value. The stable value is a user_dataset_id
   * 
   * @throws WdkUserException
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toStableValue(org.gusdb
   *      .wdk.model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, String rawValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException {
    // get type and find a proper parser for it
    DatasetParam datasetParam = (DatasetParam) param;
    String type = contextValues.get(datasetParam.getTypeSubParam());
    String uploadFile = contextValues.get(datasetParam.getFileSubParam());
    DatasetParser parser = parsers.get(type);

    // parse the content;
    List<String[]> data = parser.parse(user, rawValue);

    // save the data into database, and get user dataset id
    DatasetFactory datasetFactory = param.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.createOrGetDataset(user, data, rawValue,
        type, uploadFile);

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
      Map<String, String> contextValues) throws WdkModelException {
    int userDatasetId = Integer.valueOf(stableValue);
    DatasetFactory datasetFactory = param.getWdkModel().getDatasetFactory();
    return datasetFactory.getOriginalContent(userDatasetId);
  }

  /**
   * the internal value is an SQL that queries against the dataset values.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.
   *      wdk.model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue,
      Map<String, String> contextValues) {
    if (param.isNoTranslation()) return stableValue;

    int userDatasetId = Integer.valueOf(stableValue);
    DatasetFactory datasetFactory = param.getWdkModel().getDatasetFactory();
    return datasetFactory.getDatasetValueSql(userDatasetId);
  }

  /**
   * The signature is the dataset id, which is independent from user info.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.
   *      model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue,
      Map<String, String> contextValues) throws WdkModelException {
    int userDatasetId = Integer.valueOf(stableValue);
    DatasetFactory datasetFactory = param.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.getDataset(user, userDatasetId);
    return Integer.toString(dataset.getDatasetId());
  }

}
