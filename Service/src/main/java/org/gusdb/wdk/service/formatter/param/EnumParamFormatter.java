package org.gusdb.wdk.service.formatter.param;

import java.util.List;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONArray;
import org.json.JSONObject;

public class EnumParamFormatter extends ParamFormatter<AbstractEnumParam> {

  EnumParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    return addEnumFields(getBaseJson(spec), _param.getVocabInstance(spec));
  }

  protected JSONObject addEnumFields(JSONObject json, EnumParamVocabInstance vocabInstance) throws WdkModelException {
    return json
        .put(JsonKeys.DISPLAY_TYPE, _param.getDisplayType())
        .put(JsonKeys.MAX_SELECTED_COUNT, _param.getMaxSelectedCount())
        .put(JsonKeys.MIN_SELECTED_COUNT, _param.getMinSelectedCount())
        .put(JsonKeys.VOCABULARY, getVocabularyObject(vocabInstance));
  }

  protected Object getVocabularyObject(EnumParamVocabInstance vocabInstance) throws WdkModelException {
    List<List<String>> vocabRows = vocabInstance.getFullVocab();
    JSONArray jsonRows = new JSONArray();
    for (List<String> row : vocabRows) {
      if (row.size() != 3) throw new WdkModelException("Enum vocab includes a row that does not contain 3 columns");
      JSONArray jsonRow = new JSONArray();
      jsonRow.put(row.get(0));
      jsonRow.put(row.get(1));
      jsonRow.put(row.get(2));
      jsonRows.put(jsonRow);
    }
    return jsonRows;
  }

  @Override
  public String getParamType() {
    return _param.isMultiPick() ? JsonKeys.MULTI_VOCAB_PARAM_TYPE : JsonKeys.SINGLE_VOCAB_PARAM_TYPE;
  }

}
