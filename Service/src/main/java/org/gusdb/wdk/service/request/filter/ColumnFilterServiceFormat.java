package org.gusdb.wdk.service.request.filter;

import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSet;
import org.gusdb.wdk.model.toolbundle.filter.StandardColumnFilterConfigSetBuilder;
import org.json.JSONObject;

public final class ColumnFilterServiceFormat {

  /**
   * Parses the given JSON configuration into a {@link StandardColumnFilterConfigSetBuilder}.
   *
   * @param question
   *   Question for which column filters are being configured.  Used to provide
   *   context when validating the input configuration.
   * @param config
   *   User input JSON from the HTTP API.
   *
   * @return A constructed {@code StandardColumnFilterConfigSetBuilder} containing the
   * config data for a legal set of columns and filters.
   *
   * @throws WdkUserException
   *   if the input configuration is invalid due to:
   *   <ul>
   *   <li>Referencing unknown columns</li>
   *   <li>Referencing columns that are not filterable</li>
   *   <li>Referencing unknown filters</li>
   *   <li>Invalid input to configure a specific column</li>
   *   </ul>
   */
  public static StandardColumnFilterConfigSetBuilder parse(
    final Question question,
    final JSONObject config
  ) throws WdkUserException {
    return new FilterConfigParser(question).parse(config);
  }

  /**
   * Renders the given {@link ColumnFilterConfigSet} as JSON in the same format
   * that is expected of incoming requests.
   *
   * @param conf
   *   Configuration set to render as JSON
   *
   * @return JSON object representing the input configuration which must be
   * compatible with {@link #parse(Question, JSONObject)}.
   */
  public JSONObject format(ColumnFilterConfigSet conf) {
    JSONObject out = new JSONObject();
    conf.forEach((columnName, columnConf) -> {
      JSONObject columnJson = new JSONObject();
      columnConf.forEach((toolName, filterConf) -> {
        columnJson.put(toolName, filterConf);
      });
      out.put(columnName, columnJson);
    });
    return out;
  }
}

