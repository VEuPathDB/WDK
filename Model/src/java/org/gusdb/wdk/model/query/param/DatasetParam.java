/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.DatasetParserReference;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Dataset param represents a list of user input ids. The list is readonly, and stored in persistence
 * instance, along with other user related data.
 * 
 * A dataset param is typed, and the author has to define a recordClass as the type of the input IDs. This
 * type is required as a function for getting a snapshot of user basket and make a step from it.
 * 
 * @author xingao
 * 
 *         raw value: Dataset object;
 * 
 *         stable value: dataset id;
 * 
 *         signature: content checksum;
 * 
 *         internal data: A SQL that represents the dataset values.
 * 
 */
public class DatasetParam extends Param {

  public static final String TYPE_DATA = "data";
  public static final String TYPE_FILE = "file";
  public static final String TYPE_BASKET = "basket";
  public static final String TYPE_STRATEGY = "strategy";

  private String recordClassRef;
  private RecordClass recordClass;

  /**
   * Only used by datasetParam, determines what input type to be selected as default.
   */
  private String defaultType;

  private List<DatasetParserReference> parserReferences = new ArrayList<>();

  private Map<String, DatasetParser> parsers;

  public DatasetParam() {}

  public DatasetParam(DatasetParam param) {
    super(param);
    this.recordClass = param.recordClass;
    this.recordClassRef = param.recordClassRef;
    this.defaultType = param.defaultType;
    if (param.parserReferences != null)
      this.parserReferences = new ArrayList<>(param.parserReferences);
    if (param.parsers != null)
      this.parsers = new LinkedHashMap<>(param.parsers);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude parser references
    List<DatasetParserReference> references = new ArrayList<>();
    for (DatasetParserReference reference : parserReferences) {
      if (reference.include(projectId)) {
        String name = reference.getName();
        if (parsers.containsKey(name))
          throw new WdkModelException("parser '" + name + "' is duplicated in datasetParam " + getFullName());
        reference.excludeResources(projectId);
        references.add(reference);
      }
    }
    parserReferences = references;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    // optional recordClass Ref
    if (recordClassRef != null)
      recordClass = (RecordClass) wdkModel.resolveReference(recordClassRef);

    // get parsers
    parsers = new LinkedHashMap<>();
    // add the default parser into it first, so that it could be overridden if needed
    DatasetParser parser = new ListDatasetParser();
    parsers.put(parser.getName(), parser);
    for (DatasetParserReference reference : parserReferences) {
      parser = reference.getParser();
      parsers.put(parser.getName(), parser);
    }
    parserReferences = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#clone()
   */
  @Override
  public Param clone() {
    return new DatasetParam(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
   */
  @Override
  protected void appendJSONContent(JSONObject jsParam, boolean extra) throws JSONException {
    if (extra) {
      jsParam.put("recordClass", recordClass.getFullName());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model .user.User,
   * java.lang.String)
   */
  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    // make sure the dataset exists
    int datasetId = Integer.parseInt(stableValue);
    user.getDataset(datasetId);
  }

  /**
   * @return the recordClass
   */
  public RecordClass getRecordClass() {
    return recordClass;
  }

  /**
   * @param recordClassRef
   *          the recordClassRef to set
   */
  public void setRecordClassRef(String recordClassRef) {
    this.recordClassRef = recordClassRef;
  }

  public void setRecordClass(RecordClass recordClass) {
    this.recordClass = recordClass;
  }

  public String getDefaultType() {
    return (defaultType != null) ? defaultType : TYPE_DATA;
  }

  public void setDefaultType(String defaultType) {
    this.defaultType = defaultType;
  }

  @Override
  protected void applySuggection(ParamSuggestion suggest) {
    defaultType = ((DatasetParamSuggestion) suggest).getDefaultType();
  }

  public Collection<DatasetParser> getParsers() {
    return parsers.values();
  }

  public DatasetParser getParser(String parserName) throws WdkModelException {
    DatasetParser parser = parsers.get(parserName);
    if (parser == null)
      throw new WdkModelException("The DatasetParser '" + parserName + "' doesn't exist in datasetParam " +
          getFullName());
    return parser;
  }

  public String getTypeSubParam() {
    return getName() + "_type";
  }

  public String getFileSubParam() {
    return getName() + "_file";
  }

  public String getDataSubParam() {
    return getName() + "_data";
  }

  public String getStrategySubParam() {
    return getName() + "_strategy";
  }

  public String getParserSubParam() {
    return getName() + "_parser";
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    Dataset dataset = (Dataset) rawValue;
    String content = dataset.getContent();
    if (content.length() > truncateLength)
      content = content.substring(0, truncateLength) + "...";
    return content;
  }
}
