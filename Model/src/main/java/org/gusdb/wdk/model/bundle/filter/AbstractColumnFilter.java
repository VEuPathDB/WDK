package org.gusdb.wdk.model.bundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.bundle.ColumnFilter;
import org.gusdb.wdk.model.bundle.impl.AbstractColumnTool;
import org.json.JSONException;

import java.io.IOException;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

abstract class AbstractColumnFilter extends AbstractColumnTool implements ColumnFilter {

  @Override
  public final SqlBuilder build() throws WdkModelException {
    parseConfig(getConfig());
    return newSqlBuilder(getColumn().getName());
  }

  @Override
  public void parseConfig(JsonNode obj) {
    try {
      Jackson.readerForUpdating(genConfig()).readValue(obj);
    } catch (IOException e) {
      throw new JSONException(e);
    }
  }

  /**
   * Creates a new instance of {@link SqlBuilder} that can be called to get a
   * syntactically correct single SQL where clause predicate.
   *
   * @param col
   *   name of the column this filter applies to
   *
   * @return a configured instance of {@link SqlBuilder}
   *
   * @throws WdkModelException
   *   may be thrown if the implementing class encounters an invalid state or
   *   configuration.
   */
  protected abstract SqlBuilder newSqlBuilder(String col)
  throws WdkModelException;

  /**
   * Copies the internal settings from this column filter into the given column
   * filter.
   *
   * @param fil
   *   other filter into which the current filter's settings will be copied.
   *
   * @return the input filter
   */
  protected <T extends ColumnFilter> T copyInto(T fil) {
    return super.copyInto(fil);
  }

  // TODO: Rename me
  protected abstract Object genConfig();
}
