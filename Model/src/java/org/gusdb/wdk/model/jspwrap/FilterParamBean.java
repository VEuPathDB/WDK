package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.json.JSONObject;

public class FilterParamBean extends EnumParamBean {

  private final FilterParam filterParam;

  public FilterParamBean(FilterParam param) {
    super(param);
    filterParam = param;
  }

  /**
   * @param user
   * @return
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.query.param.FlatVocabParam#getMetadataSpec(org.gusdb.wdk.model.user.User)
   */
  public Map<String, Map<String, String>> getMetadataSpec(UserBean user) throws WdkModelException {
    return filterParam.getMetadataSpec(user.getUser());
  }

  /**
   * @param user
   * @param contextValues
   * @return
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.query.param.FlatVocabParam#getMetadata(org.gusdb.wdk.model.user.User,
   *      java.util.Map)
   */
  public Map<String, Map<String, String>> getMetadata(UserBean user, Map<String, String> contextValues)
      throws WdkModelException {
    return filterParam.getMetadata(user.getUser(), contextValues);
  }

  /**
   * @param user
   * @param contextValues
   * @return
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.query.param.AbstractEnumParam#getJSONValues(org.gusdb.wdk.model.user.User, java.util.Map)
   */
  @Override
  public JSONObject getJsonValues() throws WdkModelException {
    return filterParam.getJsonValues(user.getUser(), contextValues);
  }
}
