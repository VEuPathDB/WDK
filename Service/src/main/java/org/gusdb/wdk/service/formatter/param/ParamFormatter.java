package org.gusdb.wdk.service.formatter.param;

import static org.gusdb.fgputil.functional.Functions.mapToList;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class ParamFormatter<T extends Param> {

  protected T _param;

  ParamFormatter(T param) {
    _param = param;
  }

  /**
   * @throws WdkModelException if error occurs while collecting information
   */
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    return getBaseJson(spec);
  }

  /**
   * @return the distinct parameter type as is relevant to a client application;
   * for example, two Param subclasses that share an API may be the same type.
   */
  protected abstract String getParamType();

  /**
   * Formats this param into JSON.
   *
   * @return This param's data as JSON
   * @throws JSONException if problem generating JSON
   */
  protected <S extends ParameterContainerInstanceSpec<S>> JSONObject getBaseJson(DisplayablyValid<S> spec) {
    JSONObject pJson = new JSONObject();
    pJson.put(JsonKeys.NAME, _param.getName());
    pJson.put(JsonKeys.DISPLAY_NAME, _param.getPrompt());
    pJson.put(JsonKeys.HELP, _param.getHelp());
    pJson.put(JsonKeys.TYPE, getParamType());
    pJson.put(JsonKeys.IS_VISIBLE, _param.isVisible());
    pJson.put(JsonKeys.GROUP, _param.getGroup().getName());
    pJson.put(JsonKeys.IS_READ_ONLY, _param.isReadonly());
    pJson.put(JsonKeys.ALLOW_EMPTY_VALUE, _param.isAllowEmpty());
    pJson.put(JsonKeys.VISIBLE_HELP, _param.getVisibleHelp());
    pJson.put(JsonKeys.DEPENDENT_PARAMS, new JSONArray(
        mapToList(_param.getDependentParams(), NamedObject::getName)));
    pJson.put(JsonKeys.INITIAL_DISPLAY_VALUE,
        // convert to "external" stable value (i.e. stable value processable by clients)
        _param.getExternalStableValue(spec.get().get(_param.getName())));
    pJson.put(JsonKeys.PROPERTIES, _param.getPropertyLists());
    return pJson;
  }

}
