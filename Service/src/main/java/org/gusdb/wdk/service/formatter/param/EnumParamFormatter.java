package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONObject;

public class EnumParamFormatter extends AbstractEnumParamFormatter {

  EnumParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    EnumParamVocabInstance vocabInstance = _param.getVocabInstance(spec);
    return getBaseJson(spec)
        .put(JsonKeys.VOCABULARY, _param.getDisplayType().equals(AbstractEnumParam.DISPLAY_TREEBOX)
            ? getVocabTreeJson(vocabInstance)
            : getVocabArrayJson(vocabInstance));
  }

}
