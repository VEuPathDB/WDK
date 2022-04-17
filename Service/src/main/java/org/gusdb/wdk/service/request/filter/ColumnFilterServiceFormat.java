package org.gusdb.wdk.service.request.filter;

import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.ColumnFilterConfigSet;
import org.gusdb.wdk.model.answer.spec.ColumnFilterConfigSet.ColumnFilterConfigSetBuilder;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public final class ColumnFilterServiceFormat {

  /**
   * Parses the given JSON configuration into a {@link ColumnFilterConfigSetBuilder}.
   *
   * @param question
   *   Question for which column filters are being configured.  Used to provide
   *   context when validating the input configuration.
   * @param config
   *   User input JSON from the HTTP API.
   *
   * @return A constructed {@code ColumnFilterConfigSetBuilder} containing the
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
  public static ColumnFilterConfigSetBuilder parse(
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
    conf.entrySet().stream().forEach(columnEntry -> {
      JSONObject columnJson = new JSONObject();
      columnEntry.getValue().entrySet().stream().forEach(filterEntry -> {
        columnJson.put(filterEntry.getKey(), filterEntry.getValue());
      });
      out.put(columnEntry.getKey(), columnJson);
    });
    return out;
  }
}

