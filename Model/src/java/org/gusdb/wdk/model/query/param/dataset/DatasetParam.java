/**
 * 
 */
package org.gusdb.wdk.model.query.param.dataset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSuggestion;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Dataset param represents a list of user input ids. The list is readonly, and
 * stored in persistence instance, along with other user related data.
 * 
 * A dataset param is typed, and the author has to define a recordClass as the
 * type of the input IDs. This type is required as a function for getting a
 * snapshot of user basket and make a step from it.
 * 
 * @author xingao
 * 
 *         raw value: can be any kind of string. The default handler only
 *         support a list of values.
 * 
 *         reference value: user_dataset_id; which references to dataset_id,
 *         then to actual list of values.
 * 
 *         internal data: an SQL that represents the list of values.
 * 
 */
public class DatasetParam extends Param {

  public static final String METHOD_TEXT = "text";
  public static final String METHOD_BASKET = "basket";
  public static final String METHOD_STRATEGY = "stategy";
  
  private static final String SUB_PARAM_METHOD = "_method";
  private static final String SUB_PARAM_TEXT_INPUT = "_input";
  private static final String SUB_PARAM_TEXT_TYPE = "_type";
  
  private static final String SUB_PARAM_DATA = "_data";
  private static final String SUB_PARAM_FILE = "_file";

  /**
   * Only used by datasetParam, determines what input type to be selected as
   * default.
   */
  private String defaultType;
  
  private boolean basketEnabled = false;
  private boolean strategyEnabled = false;

  public DatasetParam() {
    setHandler(new DatasetParamHandler());
  }

  public DatasetParam(DatasetParam param) {
    super(param);
    this.defaultType = param.defaultType;
    this.basketEnabled = param.basketEnabled;
    this.strategyEnabled = param.strategyEnabled;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    // make sure the handler is a DatasetParamHandler
    if (handler == null || !(handler instanceof DatasetParamHandler))
      throw new WdkModelException("The handler for datasetParam "
          + getFullName() + " has to be DatasetParamHandler or a subclass "
          + "of it.");
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
  protected void appendJSONContent(JSONObject jsParam, boolean extra)
      throws JSONException {
    // there is nothing more to be added.
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model
   * .user.User, java.lang.String)
   */
  @Override
  protected void validateValue(User user, String dependentValue,
      Map<String, String> contextValues) throws WdkModelException {
    // try to get the dataset
    int userDatasetId = Integer.parseInt(dependentValue);
    user.getDataset(userDatasetId);
  }

  public String getDefaultType() {
    if (defaultType != null) return defaultType;
    
    // default type is not defined, use the first type in the list
    Map<String, String> types = getTypes();
    if (types.size() == 0) return null;
    return types.keySet().iterator().next();
  }

  public void setDefaultType(String defaultType) {
    this.defaultType = defaultType;
  }

  @Override
  protected void applySuggection(ParamSuggestion suggest) {
    defaultType = suggest.getDefaultType();
  }

  public String getMethodSubParam() {
    return name + SUB_PARAM_METHOD;
  }
  
  public String getTextInputSubParam() {
    return name + SUB_PARAM_TEXT_INPUT;
  }

  public String getTextTypeSubParam() {
    return name + SUB_PARAM_TEXT_TYPE;
  }
  
  public String getDataSubParam() {
    return name + SUB_PARAM_DATA;
  }
  
  public String getFileSubParam() {
    return name + SUB_PARAM_FILE;
  }

  public Map<String, String> getTextTypes() {
    Map<String, String> types = new LinkedHashMap<>();
    DatasetParamHandler datasetHandler = (DatasetParamHandler) handler;
    for (DatasetParser parser : datasetHandler.getParsers()) {
      types.put(parser.getName(), parser.getDisplay());
    }
    return types;
  }
  
  public List<String> getMethods() {
    
  }

  /**
   * @return the basketEnabled
   */
  public boolean isBasketEnabled() {
    return basketEnabled;
  }

  /**
   * @param basketEnabled the basketEnabled to set
   */
  public void setBasketEnabled(boolean basketEnabled) {
    this.basketEnabled = basketEnabled;
  }

  /**
   * @return the strategyEnabled
   */
  public boolean isStrategyEnabled() {
    return strategyEnabled;
  }

  /**
   * @param strategyEnabled the strategyEnabled to set
   */
  public void setStrategyEnabled(boolean strategyEnabled) {
    this.strategyEnabled = strategyEnabled;
  }
}
