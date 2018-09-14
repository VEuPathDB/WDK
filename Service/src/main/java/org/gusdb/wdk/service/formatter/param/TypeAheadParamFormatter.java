package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.json.JSONException;
import org.json.JSONObject;

public class TypeAheadParamFormatter extends AbstractEnumParamFormatter implements DependentParamProvider {

  TypeAheadParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    EnumParamVocabInstance vocabInstance = getVocabInstance(user, dependedParamValues);
    return super.getJson()
        .put(JsonKeys.DEFAULT_VALUE, vocabInstance.getDefaultValue())
        .put(JsonKeys.VOCABULARY, getVocabArrayJson(vocabInstance));
  }
}
