package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.gusdb.wdk.model.query.param.FilterParamHandler;
import org.json.JSONArray;
import org.json.JSONException;
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
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.query.param.FlatVocabParam#getMetadataSpec(org.gusdb.wdk.model.user.User)
   */
  public Map<String, Map<String, String>> getMetadataSpec(UserBean user) throws WdkModelException,
      WdkUserException {
    return filterParam.getMetadataSpec(user.getUser());
  }

  /**
   * @param user
   * @param contextValues
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.query.param.FlatVocabParam#getMetadata(org.gusdb.wdk.model.user.User,
   *      java.util.Map)
   */
  public Map<String, Map<String, String>> getMetadata(UserBean user, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    return filterParam.getMetadata(user.getUser(), contextValues);
  }

  /**
   * @param user
   * @param contextValues
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.query.param.AbstractEnumParam#getJSONValues(org.gusdb.wdk.model.user.User,
   *      java.util.Map)
   */
  @Override
  public JSONObject getJsonValues() throws WdkModelException, WdkUserException {
    return filterParam.getJsonValues(user.getUser(), contextValues);
  }

  @Override
  public void setStableValue(String stabletValue) throws WdkModelException {
    if (stabletValue == null)
      stabletValue = getDefault();
    this.stableValue = stabletValue;

    // also set the current values
    if (stableValue != null) {
      try {
        JSONObject jsValue = new JSONObject(stabletValue);
        JSONArray jsTerms = jsValue.getJSONArray(FilterParamHandler.TERMS_KEY);
        String[] terms = new String[jsTerms.length()];
        for (int i = 0; i < terms.length; i++) {
          terms[i] = jsTerms.getString(i);
        }
        setCurrentValues(terms);
      }
      catch (JSONException ex) {
        throw new WdkModelException(ex);
      }
    }
  }
}
