package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

public class FlatVocabParamBean extends EnumParamBean {

  private final FlatVocabParam flatVocabParam;

  public FlatVocabParamBean(FlatVocabParam param) {
    super(param);
    flatVocabParam = param;
  }

  /**
   * @param user
   * @return
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.query.param.FlatVocabParam#getMetadataSpec(org.gusdb.wdk.model.user.User)
   */
  public Map<String, Map<String, String>> getMetadataSpec(UserBean user) throws WdkModelException {
    return flatVocabParam.getMetadataSpec(user.getUser());
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
    return flatVocabParam.getMetadata(user.getUser(), contextValues);
  }

  /**
   * @param user
   * @param contextValues
   * @return
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.query.param.AbstractEnumParam#getJSONValues(org.gusdb.wdk.model.user.User, java.util.Map)
   */
  public JSONObject getJSONValues(User user, Map<String, String> contextValues) throws WdkModelException {
    return flatVocabParam.getJSONValues(user, contextValues);
  }

  public boolean isFilterParam() {
    return flatVocabParam.isFilterParam();
  }
}
