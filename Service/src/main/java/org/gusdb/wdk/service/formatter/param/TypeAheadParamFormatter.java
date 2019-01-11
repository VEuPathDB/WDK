package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONObject;

public class TypeAheadParamFormatter extends AbstractEnumParamFormatter {

  TypeAheadParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(SemanticallyValid<QueryInstanceSpec> spec) throws WdkModelException {
    EnumParamVocabInstance vocabInstance = _param.getVocabInstance(spec);
    return getBaseJson(spec)
        .put(JsonKeys.DEFAULT_VALUE, vocabInstance.getDefaultValue())
        .put(JsonKeys.VOCABULARY, getVocabArrayJson(vocabInstance));
  }
}
