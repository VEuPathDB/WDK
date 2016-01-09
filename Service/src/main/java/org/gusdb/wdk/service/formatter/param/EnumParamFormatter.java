package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.Keys;
import org.json.JSONException;
import org.json.JSONObject;

public class EnumParamFormatter extends AbstractEnumParamFormatter implements VocabProvider {

  EnumParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    EnumParamVocabInstance vocabInstance = getVocabInstance(user, dependedParamValues);
    return super.getJson()
        .put(Keys.DEFAULT_VALUE, vocabInstance.getDefaultValue())
        .put(Keys.VOCABULARY, getVocabJson(vocabInstance));
  }
}
